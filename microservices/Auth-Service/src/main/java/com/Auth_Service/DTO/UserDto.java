package com.Auth_Service.DTO;
import lombok.* ;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String email;
    private String password;
    private String role;
}