package io.github.springstudent.bean;

/**
 * @author zhouning
 * @date 2023/08/16 11:15
 */
public class ResponseEntity<T> {

    private String msg;

    private Integer code;

    private T result;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public ResponseEntity(String msg, Integer code, T result) {
        this.msg = msg;
        this.code = code;
        this.result = result;
    }

    public static <T> ResponseEntity fail(String msg, int code) {
        return new ResponseEntity(msg, code, null);
    }

    public static <T> ResponseEntity success(T result) {
        return new ResponseEntity("success", 200, result);
    }
}
