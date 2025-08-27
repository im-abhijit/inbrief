package io.github.abhijit.inbrief.api;

public class ApiResponse<T> {
    private ResultInfo resultInfo;
    private T data;

    public ApiResponse() {}

    public ApiResponse(ResultInfo resultInfo, T data) {
        this.resultInfo = resultInfo;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String code, String msg, T data) {
        return new ApiResponse<>(new ResultInfo(code, msg, "SUCCESS"), data);
    }

    public static <T> ApiResponse<T> failure(String code, String msg) {
        return new ApiResponse<>(new ResultInfo(code, msg, "FAILURE"), null);
    }

    public static <T> ApiResponse<T> success(ResultCode code, String msg, T data) {
        return success(code.getCode(), msg != null ? msg : code.getDefaultMessage(), data);
    }

    public static <T> ApiResponse<T> failure(ResultCode code, String msg) {
        return failure(code.getCode(), msg != null ? msg : code.getDefaultMessage());
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

