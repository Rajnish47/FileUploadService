package com.rajnish.FileUploadService.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import com.rajnish.FileUploadService.dto.ResumeUploadRequest;
import com.rajnish.FileUploadService.dto.ResumeUploadResponse;
import com.rajnish.FileUploadService.exception.CorruptFileException;
import com.rajnish.FileUploadService.exception.FileStorageException;
import com.rajnish.FileUploadService.model.ChunkMetadata;
import com.rajnish.FileUploadService.model.UploadType;
import com.rajnish.FileUploadService.repository.ChunkDataRepository;
import com.rajnish.FileUploadService.util.ReAssembleFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rajnish.FileUploadService.dto.UploadInitiateRequest;
import com.rajnish.FileUploadService.dto.UploadInitiateResponse;
import com.rajnish.FileUploadService.model.FileMetadata;
import com.rajnish.FileUploadService.model.UploadStatus;
import com.rajnish.FileUploadService.repository.FileMetadataRepository;
import com.rajnish.FileUploadService.service.interfaces.FileUploadService;
import com.rajnish.FileUploadService.util.CalculateChecksum;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private static final String STORAGE_DIRECTORY = "/Users/rj/Projects/data_storage";
    @Value("${app.upload.chunk-size}")
    private long chunk_size;
    @Value("${app.upload.expires-seconds}")
    private long expiresSeconds;
    private final FileMetadataRepository fileMetadataRepository;
    private final ChunkDataRepository chunkDataRepository;

    public FileUploadServiceImpl(FileMetadataRepository fileMetadataRepository,ChunkDataRepository chunkDataRepository)
    {
        this.fileMetadataRepository = fileMetadataRepository;
        this.chunkDataRepository = chunkDataRepository;
    }


    @Override
    public boolean saveFile(MultipartFile file,UUID uploadId, int chunkNo,String checksum)  {

        if(file==null)
        {
            throw new NullPointerException("No File Found!!!");
        }
        var targetFile = new File(STORAGE_DIRECTORY+ File.separator+file.getOriginalFilename());
        if(!Objects.equals(targetFile.getParent(), STORAGE_DIRECTORY))
        {
            throw new SecurityException("Unsupported filename");
        }

        Optional<ChunkMetadata> chunkMetadataOptional = chunkDataRepository.findByUploadIdAndChunkNo(uploadId,chunkNo);
        if(chunkMetadataOptional.isPresent() && chunkMetadataOptional.get().getUploadStatus()==UploadStatus.VERIFIED)
        {
            log.info("Duplicate chunk received");
            return true;
        }

        String chunkChecksum;
        try{
            chunkChecksum = CalculateChecksum.calculateChunkChecksum(file.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Unable to create checksum");
        }

        if(chunkChecksum.isEmpty() || !chunkChecksum.contentEquals(checksum))
        {
            throw  new CorruptFileException("File Mutation Detected");
        }

        ChunkMetadata chunkMetadata = new ChunkMetadata();
        chunkMetadata.setUploadId(uploadId);
        chunkMetadata.setChunkNo(chunkNo);
        chunkMetadata.setChunkPath(targetFile.getPath());
        chunkMetadata.setChecksum(checksum);
        chunkMetadata.setUploadStatus(UploadStatus.WRITING);

        ChunkMetadata savedChunkMetadata = chunkDataRepository.save(chunkMetadata);
        int expectedChunks = fileMetadataRepository.findTotalChunksByUploadId(uploadId);
        long currentChunkCount = chunkDataRepository.countByUploadId(uploadId);
        try{
            Files.copy(file.getInputStream(),targetFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
            savedChunkMetadata.setUploadStatus(UploadStatus.VERIFIED);
            chunkDataRepository.save(savedChunkMetadata);
        } catch (IOException e) {
            throw new FileStorageException("Unable to write file to storage.");
        }

        if(currentChunkCount == expectedChunks)
        {
            log.info("All chunks arrived, starting reassembly.");
            List<ChunkMetadata> chunkMetadataList = chunkDataRepository.findByUploadId(uploadId);
            FileMetadata savedFileMetadata  = fileMetadataRepository.findById(uploadId).get();
            try{
                ReAssembleFile.reAssembleFile(chunkMetadataList,savedFileMetadata.getChecksumSha256(),savedFileMetadata.getFileName(),STORAGE_DIRECTORY);
                savedFileMetadata.setStatus(UploadStatus.VERIFIED);
                fileMetadataRepository.save(savedFileMetadata);

            } catch (IOException e) {
                throw new FileStorageException("Unable to reassemble file as of now.");
            }
        }

        return true;        
    }

    @Override
    public UploadInitiateResponse initiateUpload(UploadInitiateRequest uploadInitiateRequest) {

        int totalChunks = (int) ((uploadInitiateRequest.fileSize()+chunk_size-1)/chunk_size);
        FileMetadata fileMetadata = getFileMetadata(uploadInitiateRequest, totalChunks);

        FileMetadata savedFileMetadata = fileMetadataRepository.save(fileMetadata);
        boolean chunkingRequired = totalChunks != 1;
        return new UploadInitiateResponse(savedFileMetadata.getId().toString(),chunk_size,chunkingRequired,totalChunks,expiresSeconds);
    }

    @Override
    public ResumeUploadResponse resumeUpload(ResumeUploadRequest resumeUploadRequest)
    {
        List<Integer> missingChunkNos = new ArrayList<>();
        int totalChunkNo = fileMetadataRepository.findTotalChunksByUploadId(resumeUploadRequest.uploadId());
        log.info("Total chunks: {}",totalChunkNo);
        Set<Integer> uploadedChunkNos = chunkDataRepository.findVerifiedChunks(resumeUploadRequest.uploadId(),"VERIFIED");
        log.info(uploadedChunkNos.toString());
        for(int i=1;i<=totalChunkNo;i++)
        {
            if(!uploadedChunkNos.contains(i))
            {
                missingChunkNos.add(i);
            }
        }
        log.info(missingChunkNos.toString());

        return new ResumeUploadResponse(missingChunkNos);
    }

    private static FileMetadata getFileMetadata(UploadInitiateRequest uploadInitiateRequest, int totalChunks) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(uploadInitiateRequest.fileName());
        fileMetadata.setFileSize(uploadInitiateRequest.fileSize());
        fileMetadata.setTotalChunks(totalChunks);
        fileMetadata.setStatus(UploadStatus.WRITING);
        fileMetadata.setChecksumSha256(uploadInitiateRequest.checksum());
        if(totalChunks==1)
        {
            fileMetadata.setUploadType(UploadType.DIRECT);
        }
        else
        {
            fileMetadata.setUploadType(UploadType.CHUNKED);
        }
        return fileMetadata;
    }
}
