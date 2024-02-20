package com.makeup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class PostCreateRequestDto {
    private Long memberId;
    private String title;
    private String content;
}
