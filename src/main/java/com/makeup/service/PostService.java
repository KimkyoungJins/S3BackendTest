package com.makeup.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.makeup.domain.Post;
import com.makeup.dto.PostCreateRequestDto;
import com.makeup.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final AmazonS3 s3Client;
    private final S3Service s3Service;
    private final PostRepository postRepository;

    @Value("${app.s3.bucket}")
    private String bucketName;

    // 이 메소드 수정
    public List<String> getFilesUrls() {
        ListObjectsV2Result result = s3Client.listObjectsV2(new ListObjectsV2Request().withBucketName(bucketName));
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        return objects.stream().map(obj -> s3Client.getUrl(bucketName, obj.getKey()).toString()).collect(Collectors.toList());
    }

    // 게시물 생성
    public Post createPost(PostCreateRequestDto postCreateRequestDto, MultipartFile file) {
        String url = s3Service.uploadFile(file); // 파일 업로드 로직 호출
        Post post = Post.builder()
                .title(postCreateRequestDto.getTitle())
                .content(postCreateRequestDto.getContent())
                .imageUrl(url)
                .build();
        return postRepository.save(post); // 생성된 Post 객체를 반환
    }

    //updatePost 메소드 추가
    public Post updatePost(Long id, PostCreateRequestDto postCreateRequestDto, MultipartFile file) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 없습니다."));
        String url = s3Service.uploadFile(file); // 파일 업로드 로직 호출
        post.update(postCreateRequestDto.getTitle(), postCreateRequestDto.getContent(), url);   // update 메소드 호출

        return postRepository.save(post); // 수정된 Post 객체를 반환
}   

    // DeltePost 메소드 추가
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시물이 없습니다."));
        postRepository.delete(post);
    }
}