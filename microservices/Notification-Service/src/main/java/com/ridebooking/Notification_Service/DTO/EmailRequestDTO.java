package com.ridebooking.Notification_Service.DTO;

import lombok.Data;

@Data
public class EmailRequestDTO {
    private String to;
    private String subject;
    private String body;
}
