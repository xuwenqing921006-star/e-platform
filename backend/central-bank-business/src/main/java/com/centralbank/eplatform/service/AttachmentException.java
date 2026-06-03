package com.centralbank.eplatform.service;

public class AttachmentException extends RuntimeException
{
    private final int statusCode;

    public AttachmentException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode()
    {
        return statusCode;
    }
}
