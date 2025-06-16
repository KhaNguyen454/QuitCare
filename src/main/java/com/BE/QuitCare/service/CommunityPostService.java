package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.CommunityPostDTO;
import com.BE.QuitCare.entity.CommunityPost;
import com.BE.QuitCare.repository.CommunityPostRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityPostService {

    @Autowired
    private CommunityPostRepository repository;

    // Convert Entity -> DTO
    private CommunityPostDTO toDTO(CommunityPost post) {
        CommunityPostDTO dto = new CommunityPostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCommentStatus(post.getCommentStatus());
        dto.setCreateAt(post.getCreateAt());
        return dto;
    }

    // Convert DTO -> Entity
    private CommunityPost toEntity(CommunityPostDTO dto) {
        CommunityPost post = new CommunityPost();
        post.setId(dto.getId());
        post.setContent(dto.getContent());
        post.setCommentStatus(dto.getCommentStatus());
        post.setCreateAt(dto.getCreateAt());
        return post;
    }

    public List<CommunityPostDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CommunityPostDTO> getById(Long id) {
        return repository.findById(id)
                .map(this::toDTO);
    }

    public CommunityPostDTO create(CommunityPostDTO dto) {
        CommunityPost entity = toEntity(dto);
        return toDTO(repository.save(entity));
    }

    public CommunityPostDTO update(Long id, CommunityPostDTO dto) {
        Optional<CommunityPost> optional = repository.findById(id);
        if (optional.isPresent()) {
            CommunityPost post = optional.get();
            post.setContent(dto.getContent());
            post.setCommentStatus(dto.getCommentStatus());
            return toDTO(repository.save(post));
        }
        return null;
    }

    public boolean softDelete(Long id) {
        Optional<CommunityPost> optional = repository.findById(id);
        if (optional.isPresent()) {
            repository.deleteById(id); // Nếu cần soft-delete thực sự → thêm trường `deleted` vào Entity
            return true;
        }
        return false;
    }
}
