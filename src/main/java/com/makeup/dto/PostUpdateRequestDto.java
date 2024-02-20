package com.makeup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostUpdateRequestDto {
    private String title;
    private String content;
    private String text;
}
