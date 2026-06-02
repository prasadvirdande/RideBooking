package com.ridebooking.user_service.DTO;

import lombok.Data;

@Data
public class UpdateUserRequestDto {

    private String firstName;

    private String lastName;

    private String phone;

    private String email;

    private String profileImageUrl;

}