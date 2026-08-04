package com.Auth_Service.ExceptionHandling;

public class Usernotfound  extends RuntimeException{

    public Usernotfound(String message) {
        super(message);
    }
}
