package com.rajnish.FileUploadService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name="upload_metadata")
public class FileMetadata extends BaseModel {

    private String fileName;
    private long fileSize;
    private int totalChunks;
    @Enumerated(EnumType.STRING)
    private UploadStatus status;
    @Enumerated(EnumType.STRING)
    private UploadType uploadType;
    @Column(name = "checksum_sha256", length = 64, nullable = false, updatable = false)
    private String checksumSha256;
}
