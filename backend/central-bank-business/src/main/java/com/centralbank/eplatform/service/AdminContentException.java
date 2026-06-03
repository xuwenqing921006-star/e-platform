package com.centralbank.eplatform.service;

public class AdminContentException extends RuntimeException
{
    private final int statusCode;

    public AdminContentException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode()
    {
        return statusCode;
    }
}
