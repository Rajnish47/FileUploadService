package com.rajnish.FileUploadService.dto;

public record UploadInitiateRequest(String fileName, long fileSize,String checksum) { }
