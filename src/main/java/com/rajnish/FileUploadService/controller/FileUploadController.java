package com.rajnish.FileUploadService.controller;

import java.io.IOException;
import java.util.UUID;

import com.rajnish.FileUploadService.dto.ChunkMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rajnish.FileUploadService.dto.UploadInitiateRequest;
import com.rajnish.FileUploadService.dto.UploadInitiateResponse;
import com.rajnish.FileUploadService.service.interfaces.FileUploadService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService)
    {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<UploadInitiateResponse> initiateFileUpload(@RequestBody UploadInitiateRequest uploadInitiateRequest)
    {
        return new ResponseEntity<>(fileUploadService.initiateUpload(uploadInitiateRequest),HttpStatus.ACCEPTED);
    }

    @PostMapping("/{uploadId}/chunk/{chunkNo}")
    public ResponseEntity<Boolean> fileUpload(@RequestPart("file") MultipartFile file, @RequestPart("chunkMetadata") ChunkMetadata chunkMetadata, @PathVariable("uploadId") UUID uploadId, @PathVariable("chunkNo")int chunkNo) {

        try {
            fileUploadService.saveFile(file,uploadId,chunkNo,chunkMetadata.checksum());
            return new ResponseEntity<>(true,HttpStatus.OK);         
        } catch (IOException e) {
            log.error("Exception during file-upload ",e);
        }

        return new ResponseEntity<>(false,HttpStatus.UNPROCESSABLE_ENTITY);
    }

//    @PostMapping("/uploadwhole")
//    public ResponseEntity<Boolean> fileUploadWhole(@RequestParam("file") MultipartFile file) {
//
//        try {
//            fileUploadService.saveFile(file);
//            return new ResponseEntity<>(true,HttpStatus.OK);
//        } catch (IOException e) {
//            log.error("Exception during file-upload ",e);
//        }
//
//        return new ResponseEntity<>(false,HttpStatus.UNPROCESSABLE_ENTITY);
//    }
}
