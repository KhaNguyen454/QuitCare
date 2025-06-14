package com.BE.QuitCare.api;

import com.BE.QuitCare.entity.CommunityPost;
import com.BE.QuitCare.service.CommunityPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class CommunityPostAPI {

    private final CommunityPostService communityPostService;

    @GetMapping
    public ResponseEntity<List<CommunityPost>> getAllPosts() {
        return ResponseEntity.ok(communityPostService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityPost> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(communityPostService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<CommunityPost> createPost(@RequestBody CommunityPost post) {
        return ResponseEntity.ok(communityPostService.createPost(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommunityPost> updatePost(@PathVariable Long id, @RequestBody CommunityPost post) {
        return ResponseEntity.ok(communityPostService.updatePost(id, post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        communityPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
