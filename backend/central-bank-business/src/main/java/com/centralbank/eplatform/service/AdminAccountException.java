package com.centralbank.eplatform.service;

public class AdminAccountException extends RuntimeException
{
    private final int statusCode;

    public AdminAccountException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode()
    {
        return statusCode;
    }
}
