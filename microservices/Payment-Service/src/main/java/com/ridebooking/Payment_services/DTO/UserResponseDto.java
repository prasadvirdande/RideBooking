package com.ridebooking.Payment_services.DTO;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;
}