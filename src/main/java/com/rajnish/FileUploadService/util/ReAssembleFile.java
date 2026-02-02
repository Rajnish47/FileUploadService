package com.rajnish.FileUploadService.util;

import java.io.*;

import com.rajnish.FileUploadService.exception.CorruptFileException;
import com.rajnish.FileUploadService.exception.FileNotFoundInStorageException;
import com.rajnish.FileUploadService.model.ChunkMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ReAssembleFile {

    public static void reAssembleFile(List<ChunkMetadata> chunkMetadataList, String checksum, String fileName) throws IOException {

        chunkMetadataList.sort(new ChunksDataChunkNoComparator());
        try(FileOutputStream fout = new FileOutputStream("/Users/rj/Projects/data_storage/"+fileName,true))
        {
            for(ChunkMetadata chunkMetadata : chunkMetadataList)
            {
                File file = new File(chunkMetadata.getChunkPath());
                try(FileInputStream fin = new FileInputStream(file))
                {
                    byte[] bytes = fin.readAllBytes();
                    fout.write(bytes);
                }
                catch (FileNotFoundException e)
                {
                    throw new FileNotFoundInStorageException("Chunk no "+chunkMetadata.getChunkNo()+" is not found in storage");

                }
            }
        }

        File file = new File("/Users/rj/Projects/data_storage/"+fileName);
        String finalChecksum;
        try{
            finalChecksum = CalculateChecksum.calculateFileChecksum(file.toPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Final checksum: {}", finalChecksum);
        log.info("Current checksum: {}", checksum);

        if(!finalChecksum.contentEquals(checksum))
        {
            throw new CorruptFileException("Checksum validation failed");
        }

        log.info("Reassembly finished!!!!!");
    }
}
