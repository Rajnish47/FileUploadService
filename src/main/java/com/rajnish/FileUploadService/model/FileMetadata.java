package com.rajnish.FileUploadService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name="upload_metadata")
public class FileMetadata extends BaseModel {

    private String fileName;
    private long fileSize;
    private int totalChunks;
    private UploadStatus status;
    private UploadType uploadType;
    @Column(name = "checksum_sha256", length = 64, nullable = false, updatable = false)
    private String checksumSha256;
}
