package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.CommunityPost;
import com.BE.QuitCare.repository.CommunityPostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;

    public List<CommunityPost> getAllPosts() {
        return communityPostRepository.findAll();
    }

    public CommunityPost getPostById(Long id) {
        return communityPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

    public CommunityPost createPost(CommunityPost post) {
        return communityPostRepository.save(post);
    }

    public CommunityPost updatePost(Long id, CommunityPost updatedPost) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));

        post.setContent(updatedPost.getContent());
        post.setCommentStatus(updatedPost.getCommentStatus());

        return communityPostRepository.save(post);
    }

    public void deletePost(Long id) {
        if (!communityPostRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found with id: " + id);
        }
        communityPostRepository.deleteById(id);
    }
}
