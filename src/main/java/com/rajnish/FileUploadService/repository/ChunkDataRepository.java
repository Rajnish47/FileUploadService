package com.rajnish.FileUploadService.repository;

import com.rajnish.FileUploadService.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ChunkDataRepository extends JpaRepository<ChunkMetadata, UUID> {

    long countByUploadId(UUID uploadId);
    List<ChunkMetadata> findByUploadId(UUID uploadId);
    Optional<ChunkMetadata> findByUploadIdAndChunkNo(UUID uploadId,int chunkNo);
    @Query(
            value = """
    SELECT chunk_no
    FROM chunk_metadata
    WHERE upload_id = :uploadId
      AND upload_status = :status
  """,
            nativeQuery = true
    )
    Set<Integer> findVerifiedChunks(
            @Param("uploadId") UUID uploadId,
            @Param("status") String status
    );
}
