package com.rajnish.FileUploadService.service.interfaces;

import java.io.IOException;
import java.util.UUID;

import com.rajnish.FileUploadService.dto.ResumeUploadRequest;
import com.rajnish.FileUploadService.dto.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import com.rajnish.FileUploadService.dto.UploadInitiateRequest;
import com.rajnish.FileUploadService.dto.UploadInitiateResponse;

public interface FileUploadService {

    public UploadInitiateResponse initiateUpload(UploadInitiateRequest uploadInitiateRequest);
    public boolean saveFile(MultipartFile file, UUID uploadId, int chunkNo,String checksum) throws IOException;
    public ResumeUploadResponse resumeUpload(ResumeUploadRequest resumeUploadRequest);
}
