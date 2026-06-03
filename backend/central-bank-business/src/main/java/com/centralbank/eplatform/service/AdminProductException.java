package com.centralbank.eplatform.service;

public class AdminProductException extends RuntimeException
{
    private final int statusCode;

    public AdminProductException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode()
    {
        return statusCode;
    }
}
