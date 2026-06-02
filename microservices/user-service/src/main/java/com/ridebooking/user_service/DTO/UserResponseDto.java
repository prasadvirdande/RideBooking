package com.ridebooking.user_service.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

    @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public  class UserResponseDto {

            private UUID id;

            private String firstName;

            private String lastName;

            private String email;

            private String phone;

            private  String password;
            private String role;

            private String status;

            private LocalDateTime createdAt;

        }

