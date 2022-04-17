package cn.sdutcs.mqtt.panel.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private int code;
    private String message;
    private T result;

    public Result(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.result = data;
    }

    public static final Result<Object> SUCCESS = new Result<>(0, "success");
    public static final Result<Object> FAIL = new Result<>(-200, "failed");

    public static Result<Object> success() {
        return SUCCESS;
    }

    public static Result<Object> success(Object data) {
        Result<Object> result = new Result<>(0, "success");
        result.setResult(data);
        return result;
    }

    public static Result<Object> failure() {
        return FAIL;
    }

    public static Result<Object> failure(Object data) {
        Result<Object> result = new Result<>(-200, "failed");
        result.setResult(data);
        return result;
    }
}