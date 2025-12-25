package org.sacabam.sacabamclickerbe.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Integer status;
    private String message;
    private T data;
    private String errorCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    // Success response
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(201, message, data, null, LocalDateTime.now());
    }

    // Error response
    public static <T> ApiResponse<T> error(Integer status, String message, String errorCode) {
        return new ApiResponse<>(status, message, null, errorCode, LocalDateTime.now());
    }
}