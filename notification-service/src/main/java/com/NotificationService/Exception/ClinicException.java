package com.NotificationService.Exception;

import org.springframework.http.HttpStatus;

public class ClinicException extends RuntimeException {

        private final HttpStatus status;

        public ClinicException(String message, HttpStatus status) {
            super(message);
            this.status = status;
        }
}
