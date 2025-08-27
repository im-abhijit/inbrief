package io.github.abhijit.inbrief.api;

public class ResultInfo {
    private String resultCodeId;
    private String resultMsg;
    private String resultStatus;

    public ResultInfo() {}

    public ResultInfo(String resultCodeId, String resultMsg, String resultStatus) {
        this.resultCodeId = resultCodeId;
        this.resultMsg = resultMsg;
        this.resultStatus = resultStatus;
    }

    public String getResultCodeId() {
        return resultCodeId;
    }

    public void setResultCodeId(String resultCodeId) {
        this.resultCodeId = resultCodeId;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}

