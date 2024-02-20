package com.makeup.controller;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.makeup.domain.Post;
import com.makeup.dto.ImageDto;
import com.makeup.dto.PostCreateRequestDto;
import com.makeup.dto.PostUpdateRequestDto;
import com.makeup.response.BaseResponse;
import com.makeup.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController()
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService awsS3Service;
    private final AmazonS3 s3Client;
    private final PostService postService;

    @Value("${app.s3.bucket}")
    private String bucketName;


//    @GetMapping("/images/main")
//    public ResponseEntity<?> listImages() {
//        List<ImageInfo> imageInfos = awsS3Service.getFilesUrls();
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("urls", imageInfos);
//        return ResponseEntity.ok(responseBody);
//    }

//     메인화면 이미지 목록을 조회함
//    @GetMapping("/images/main")
//    public ResponseEntity<List<String>> listImages() {
//        List<String> urls = awsS3Service.getFilesUrls();
//        return ResponseEntity.ok(urls);
//    }
@GetMapping("/images")
public BaseResponse<List<ImageDto>> listImages() {
    ListObjectsV2Result result = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName));
    List<S3ObjectSummary> objects = result.getObjectSummaries();

    List<ImageDto> imageDtos = new ArrayList<>();
    String[] texts = {"#취준생", "#가을뮤트", "#여름쿨톤",
            "#고양이메이크업", "#봄웜톤", "#데이트",
            "#봄웜톤", "#여름쿨톤", "#취준생", "#취준생",
            "#데이트", "#취준생", "#깔끔하게", "#깔끔하게",
            "#한복메이크업", "#발랄"};

    for (int i = 0; i < objects.size(); i++) {
        S3ObjectSummary obj = objects.get(i);
        ImageDto dto = new ImageDto();
        dto.setImageId(String.valueOf(i + 1)); // 예시로 id를 순서대로 설정
        dto.setPostId("100" + (i + 1)); // postId 설정 예시
        dto.setUserId("user" + (100 + i)); // userId 설정 예시
        dto.setImageUrl(s3Client.getUrl(bucketName, obj.getKey()).toString()); // 이미지 URL 설정
        dto.setText(texts[i % texts.length]); // 텍스트 순환적으로 설정
        imageDtos.add(dto);
    }

    return BaseResponse.success(imageDtos);
}
    // 게시물 등록
    @PostMapping("/")
    public BaseResponse<?> createPost(@RequestPart("json") PostCreateRequestDto postCreateRequestDto,
                                        @RequestPart("file") MultipartFile file) {
//        try {
            Post post = postService.createPost(postCreateRequestDto, file); // 수정된 서비스 메서드 호출

            return BaseResponse.success(post);
//        }
//        catch (Exception e) {
//            // 실패 응답 구성
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("status", "error");
//            errorResponse.put("message", "게시물 생성 중 오류 발생");
//            errorResponse.put("error", Map.of("message", e.getMessage()));
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
    }

    // 게시물 삭제로직
    @DeleteMapping("/{postId}")
    public BaseResponse<?> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return BaseResponse.ok();
        } catch (Exception e) {
            return BaseResponse.error(e.getMessage());
        }
    }

    // 게시물 업데이트 로직
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                       @RequestPart("json") PostUpdateRequestDto requestDto,
                                       @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            Post post = postService.updatePost(postId, requestDto, file);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("message", "게시물이 수정되었습니다.");
            responseBody.put("data", Map.of(
                    "postId", post.getPostId(),
                    "title", post.getTitle(),
                    "content", post.getContent(),
                    "imageUrl", post.getImageUrl()
            ));
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "게시물 수정 중 오류 발생");
            errorResponse.put("error", Map.of("message", e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


}

