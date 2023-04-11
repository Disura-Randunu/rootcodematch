package com.disura.rootcodematch.controller;

public class AppResponse {

    private final Boolean status;
    private final String message;
    private final Object data;

    public AppResponse(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
