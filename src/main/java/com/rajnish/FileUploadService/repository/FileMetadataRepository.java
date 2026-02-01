package com.rajnish.FileUploadService.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rajnish.FileUploadService.model.FileMetadata;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    @Query("select f.totalChunks from upload_metadata f where f.id = :uploadId")
    int findTotalChunksByUploadId(@Param("uploadId") UUID uploadId);
    
}
