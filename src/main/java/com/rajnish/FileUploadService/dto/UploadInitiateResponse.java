package com.rajnish.FileUploadService.dto;

public record UploadInitiateResponse(
    String uploadId,
    long chunkSize,
    boolean chunkingRequired,
    int totalParts,
    long expiresInSeconds
) { }

