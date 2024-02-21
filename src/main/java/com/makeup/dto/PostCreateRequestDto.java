package com.makeup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class PostCreateRequestDto {
    private Long memberId;
    private String title;
    private String brandName; // 변경됨
    private String cosName;   // 변경됨
    private String text;      // 필요에 따라 유지 또는 제거
}