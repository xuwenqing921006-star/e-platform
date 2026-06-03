package com.centralbank.eplatform.service;

public interface AuditLogRecorder
{
    void record(String operationType, String objectType, String objectName, String description);

    static AuditLogRecorder noop()
    {
        return (operationType, objectType, objectName, description) -> {
        };
    }
}
