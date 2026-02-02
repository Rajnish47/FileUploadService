package com.rajnish.FileUploadService.exception;

public class FileNotFoundInStorageException extends RuntimeException{

    public FileNotFoundInStorageException(String message)
    {
        super(message);
    }
}
