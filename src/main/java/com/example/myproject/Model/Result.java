package com.example.myproject.Model;

//创造一个Result类，用于返回结果，包含code，message，data,其中data为泛型

public class Result<T> {
    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static Result success() {
        return new Result(200, "操作成功", null);
    }

    public static <E> Result<E> success(E data) {
        return new Result<>(200, "操作成功", data);
    }

    public static Result failure(String message) {
        return new Result(400, message, null);
    }
}

/*
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static Result success() {
        return new Result(200, "操作成功", null);
    }
    public static <E> Result <E> success(E data) {
        return new Result<>(200, "操作成功", data);
    }
    public static Result failure(String message) {
        return new Result(400, message, null);
    }
}
*/