package com.rajnish.FileUploadService.repository;

import com.rajnish.FileUploadService.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChunkDataRepository extends JpaRepository<ChunkMetadata, UUID> {

    long countByUploadId(UUID uploadId);
    List<ChunkMetadata> findByUploadId(UUID uploadId);
    Optional<ChunkMetadata> findByUploadIdAndChunkNo(UUID uploadId,int chunkNo);
}
