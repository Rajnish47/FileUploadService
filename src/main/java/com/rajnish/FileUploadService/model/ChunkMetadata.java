package com.rajnish.FileUploadService.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class ChunkMetadata extends BaseModel{

    private UUID uploadId;
    private int chunkNo;
    private String chunkPath;
    private String checksum;
}
