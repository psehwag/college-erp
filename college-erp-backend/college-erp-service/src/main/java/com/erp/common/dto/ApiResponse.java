package com.erp.common.dto;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Long total;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, Long total) {
        this.success = success;
        this.message = message;
        this.data    = data;
        this.total   = total;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data, long total) {
        return new ApiResponse<>(true, message, data, total);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
}
