package com.rajnish.FileUploadService.exception;

public class CorruptFileException extends RuntimeException{

    public CorruptFileException(String message)
    {
        super(message);
    }
}
