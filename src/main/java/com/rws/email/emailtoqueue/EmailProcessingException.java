package com.rws.email.emailtoqueue;

class EmailProcessingException extends RuntimeException {
    EmailProcessingException(String message, Exception e) {
        super(message, e);
    }
}
