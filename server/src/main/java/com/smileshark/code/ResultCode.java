package com.smileshark.code;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "成功"),
    ADD_SUCCESS(200, "添加成功"),


    FAIL(500, "失败"),
    NOT_FOUND(404, "未找到"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_ACCEPTABLE(406, "不接受"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "冲突"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过多"),
    DELETE_SUCCESS(200, "删除成功"),
    UPDATE_SUCCESS(200, "修改成功"),
    USER_NOT_EXIST(404, "用户不存在"),
    PASSWORD_ERROR(501, "密码错误"),
    LOGIN_SUCCESS(200, "登录成功"), ADD_ERROR(500, "保存失败"), DELETE_ERROR(500, "删除失败"), UPDATE_ERROR(500, "修改失败"), GET_SUCCESS(200, "查询成功");
    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
