package com.disura.rootcodematch.error.match;

public class MatchException extends Exception {
    public MatchException() {
        super();
    }

    public MatchException(String message) {
        super(message);
    }

    public MatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public MatchException(Throwable cause) {
        super(cause);
    }

    protected MatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
