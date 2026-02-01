package com.rajnish.FileUploadService.service;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.rajnish.FileUploadService.model.ChunkData;
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
    public boolean saveFile(MultipartFile file,UUID uploadId, int chunkNo,String checksum) throws IOException {

        if(file==null)
        {
            throw new NullPointerException("No File Found!!!");
        }
        var targetFile = new File(STORAGE_DIRECTORY+ File.separator+file.getOriginalFilename());
        if(!Objects.equals(targetFile.getParent(), STORAGE_DIRECTORY))
        {
            throw new SecurityException("Unsuppourted filename");
        }

        String chunkChencksum;
        try{

            chunkChencksum = CalculateChecksum.calculateChunkChecksum(file.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Chunk Checksum: "+chunkChencksum);
        if(chunkChencksum.isEmpty() || !chunkChencksum.contentEquals(checksum))
        {
            return  false;
        }

        ChunkData chunkData = new ChunkData();
        chunkData.setUploadId(uploadId);
        chunkData.setChunkNo(chunkNo);
        chunkData.setChunkPath(targetFile.getPath());
        chunkData.setChecksum(checksum);

        chunkDataRepository.save(chunkData);
        int expectedChunks = fileMetadataRepository.findTotalChunksByUploadId(uploadId);
        long currentChunkCount = chunkDataRepository.countByUploadId(uploadId);
        Files.copy(file.getInputStream(),targetFile.toPath(),StandardCopyOption.REPLACE_EXISTING);

        if(currentChunkCount == expectedChunks)
        {
            log.info("All chunks arrived, starting reassembly");
            List<ChunkData> chunkDataList = chunkDataRepository.findByUploadId(uploadId);
            ReAssembleFile.reAssembleFile(chunkDataList);
        }

        return true;        
    }

    @Override
    public UploadInitiateResponse initiateUpload(UploadInitiateRequest uploadInitiateRequest) {

        int totalChunks = (int) ((uploadInitiateRequest.fileSize()+chunk_size-1)/chunk_size);
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(uploadInitiateRequest.fileName());
        fileMetadata.setFileSize(uploadInitiateRequest.fileSize());
        fileMetadata.setTotalChunks(totalChunks);
        fileMetadata.setStatus(UploadStatus.INITIATED);
        fileMetadata.setChecksumSha256(uploadInitiateRequest.checksum());
        if(totalChunks==1)
        {
            fileMetadata.setUploadType(UploadType.DIRECT);
        }
        else
        {
            fileMetadata.setUploadType(UploadType.CHUNKED);
        }

        FileMetadata savedFileMetadata = fileMetadataRepository.save(fileMetadata);
        UploadInitiateResponse uploadInitiateResponse = new UploadInitiateResponse(savedFileMetadata.getId().toString(),chunk_size,totalChunks,expiresSeconds);

        return uploadInitiateResponse;
    }

    //non interface methods

    public boolean saveChunks(MultipartFile file) throws  IOException
    {
        if(file==null)
        {
            throw new NullPointerException("No File Found!!!");
        }
        var targetFile = new File(STORAGE_DIRECTORY+File.separator+file.getOriginalFilename());
        if(!Objects.equals(targetFile.getParent(), STORAGE_DIRECTORY))
        {
            throw new SecurityException("Unsuppourted filename");
        }

        Files.copy(file.getInputStream(),targetFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
        return true; 
    }
}
