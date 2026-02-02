package com.rajnish.FileUploadService.controller.handler;

import com.rajnish.FileUploadService.controller.FileUploadController;
import com.rajnish.FileUploadService.exception.CorruptFileException;
import com.rajnish.FileUploadService.exception.FileNotFoundInStorageException;
import com.rajnish.FileUploadService.exception.FileStorageException;
import com.rajnish.FileUploadService.repository.ExceptionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackageClasses = FileUploadController.class)
public class FileUploadControllerHandler {

    @ExceptionHandler(CorruptFileException.class)
    public ResponseEntity<ExceptionResponseDTO> corruptFileExceptionHandler(CorruptFileException e)
    {
        return new ResponseEntity<>(new ExceptionResponseDTO(e.getMessage(), 422), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(FileNotFoundInStorageException.class)
    public ResponseEntity<ExceptionResponseDTO> fileNotFoundInStorageExceptionHandler(FileNotFoundInStorageException e)
    {
        return new ResponseEntity<>(new ExceptionResponseDTO(e.getMessage(),404),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ExceptionResponseDTO> fileStorageExceptionHandler(FileStorageException e)
    {
        return new ResponseEntity<>(new ExceptionResponseDTO(e.getMessage(),500),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
