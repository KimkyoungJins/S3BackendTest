package com.makeup.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private String imageId;
    private String postId;
    private String userId;
    private String imageUrl;
    private String text;
}