package cn.sdutcs.mqtt.broker.web.model;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public Result(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

    public static final Result<Object> SUCCESS = new Result<>(200, "success");
    public static final Result<Object> FAIL = new Result<>(-200, "failed");

    public static Result<Object> success() {
        return SUCCESS;
    }

    public static Result<Object> success(Object data) {
        Result<Object> result = new Result<>(200, "success");
        result.setData(data);
        return result;
    }

    public static Result<Object> failure() {
        return FAIL;
    }

    public static Result<Object> failure(Object data) {
        Result<Object> result = new Result<>(-200, "failed");
        result.setData(data);
        return result;
    }
}