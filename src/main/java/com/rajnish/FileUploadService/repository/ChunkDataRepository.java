package com.rajnish.FileUploadService.repository;

import com.rajnish.FileUploadService.model.ChunkData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChunkDataRepository extends JpaRepository<ChunkData, UUID> {

    long countByUploadId(UUID uploadId);
    List<ChunkData> findByUploadId(UUID uploadId);
}
