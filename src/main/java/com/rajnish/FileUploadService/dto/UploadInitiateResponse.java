package com.rajnish.FileUploadService.dto;

public record UploadInitiateResponse(
    String uploadId,
    long chunkSize,
    int totalParts,
    long expiresInSeconds
) { }

