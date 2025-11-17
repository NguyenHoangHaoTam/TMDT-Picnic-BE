package com.java.TMDTPicnic.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1000, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(1001, "Invalid refresh token" , HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1002, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1003, "Email already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(1004, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1005, "Email not found", HttpStatus.NOT_FOUND),
    OLD_PASSWORD_INCORRECT(1005, "Old password incorrect", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1006, "Invalid password", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1007, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    ;



    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
