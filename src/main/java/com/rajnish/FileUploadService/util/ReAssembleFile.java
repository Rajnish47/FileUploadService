package com.rajnish.FileUploadService.util;

import java.io.File;

import com.rajnish.FileUploadService.model.ChunkData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class ReAssembleFile {

    public static boolean reAssembleFile(List<ChunkData> chunkDataList) throws IOException {

        chunkDataList.sort(new ChunksDataChunkNoComparator());
        try(FileOutputStream fout = new FileOutputStream("/Users/rj/Projects/data_storage/movie.mkv",true))
        {
            for(ChunkData chunkData: chunkDataList)
            {
                File file = new File(chunkData.getChunkPath());
                FileInputStream fin = new FileInputStream(file);
                byte[] bytes = fin.readAllBytes();

                fout.write(bytes);
            }

        }

        log.info("Reassembly finished!!!!!");

        return true;
    }
}
