package org.nott.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Nott
 * @date 2024-5-7
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T extends Object> implements Serializable {
    private int code;
    private String msg;
    private T obj;

    public R(String msg, T obj) {
        this.msg = msg;
        this.obj = obj;
    }

    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(String msg) {
        this.msg = msg;
    }

    public static R<?> okData(String msg, Object obj) {
        return new R<>(msg, obj);
    }

    public static R<?> ok(String msg) {
        return new R<>(msg);
    }

    public static R<?> ok() {
        return new R<>("success");
    }

    public static R<?> okData(Object obj) {
        return new R<>(200, "success", obj);
    }

    public static R<?> failure(String msg) {
        return new R<>(-999, msg);
    }

    public static R<?> failure() {
        return new R<>(-999, "failure");
    }
}
