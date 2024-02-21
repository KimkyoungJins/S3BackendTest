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


    // 이미지랑 텍스트 매치를 해야한다.
    List<ImageDto> imageDtos = new ArrayList<>();
    String[] texts = {"한번에 취뽀하고 싶을 때",
            "아이&립 만으로 가을느낌 내고 싶을 때",
            "더운 날 깔끔한 메이크업으로 기분 전환",
            "눈꼬리를 높여 고양이가 되세요.",
            "벚꽃보러 봄 메이크업을 해보는 건 어떨까요?",
            "발그레 블러셔로 데이트 느낌을 줘보세요.",
            "복숭아 메이크업을 해보자요",
            "일자 눈썹을 해보자요",
            "확실하게 면접에서 눈길을 잡자",
            "가을 웜톤인듯 아닌듯 분위기있게",
            "여리여리한 눈매를 강조",
            "착한 고양이상으로 변신",
            "메이크업은 여배우처럼",
            "뮤트하게 꾸안꾸",
            "한복 입고 데이트갈 때",
            "단발하고 처음 메이크업할 때"};

    for (int i = 0; i < objects.size(); i++) {
        S3ObjectSummary obj = objects.get(i);
        ImageDto dto = new ImageDto();
        dto.setImageId(String.valueOf(i + 1)); // 예시로 id를 순서대로 설정
        dto.setPostId("100" + (i + 1)); // postId 설정 예시
        dto.setUserId("user" + (100 + i)); // userId 설정 예시
        System.out.println("obj.getKey().toString()=" + obj.getKey().toString());
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

