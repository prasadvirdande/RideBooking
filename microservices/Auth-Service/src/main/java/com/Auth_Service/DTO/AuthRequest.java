package com.Auth_Service.DTO;

import lombok.Data;

@Data
public class AuthRequest {
    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String phoneNumber;

    private String profilePicture;

    private String defaultAddress;


}
