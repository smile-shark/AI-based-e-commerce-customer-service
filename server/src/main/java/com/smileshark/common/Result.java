package com.smileshark.common;

import com.smileshark.code.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static Result<?> success(ResultCode resultCode) {
        return normal(resultCode);
    }

    public static Result<?> error(ResultCode resultCode) {
        return normal(resultCode);
    }

    public static <T> Result<T> success(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(data)
                .build();
    }

    public static Result<?> normal(ResultCode resultCode) {
        return Result.builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .build();
    }
}
