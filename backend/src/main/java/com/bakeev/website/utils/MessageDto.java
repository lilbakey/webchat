package com.bakeev.website.utils;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageDto {

    private Long id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    private Long fileId;
    private String attachmentName;
    private Long attachmentId;

}
