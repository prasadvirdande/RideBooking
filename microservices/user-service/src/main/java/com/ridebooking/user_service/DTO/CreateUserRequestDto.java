package com.ridebooking.user_service.DTO;

import lombok.Data;

@Data
public class CreateUserRequestDto {

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String phoneNumber;

    private String profilePicture;

    private String defaultAddress;
}