package com.disura.rootcodematch.controller;

public class AppResponse {

    private Boolean status;
    private String message;
    private Object data;

    public AppResponse(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
