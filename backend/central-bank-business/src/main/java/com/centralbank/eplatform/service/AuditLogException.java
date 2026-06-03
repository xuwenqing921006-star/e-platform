package com.centralbank.eplatform.service;

public class AuditLogException extends RuntimeException
{
    private final int statusCode;

    public AuditLogException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public int statusCode()
    {
        return statusCode;
    }
}
