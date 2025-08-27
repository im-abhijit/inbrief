package io.github.abhijit.inbrief.api;

public enum ResultCode {
    SUCCESS("000", "Success"),
    BAD_REQUEST("001", "Bad request"),
    VALIDATION_FAILED("002", "Validation failed"),
    MISSING_PARAMETER("003", "Missing parameter"),
    NOT_FOUND("004", "Not found"),
    METHOD_NOT_ALLOWED("005", "Method not allowed"),
    UNSUPPORTED_MEDIA_TYPE("006", "Unsupported media type"),
    TYPE_MISMATCH("007", "Type mismatch"),
    MALFORMED_JSON("008", "Malformed JSON"),
    NO_HANDLER("404", "Endpoint not found"),
    NO_MORE_ARTICLES("011", "No more articles"),
    SERVICE_UNAVAILABLE("010", "Service unavailable"),
    INTERNAL_ERROR("999", "Internal server error");

    private final String code;
    private final String defaultMessage;

    ResultCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

