package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.CommentDTO;
import com.BE.QuitCare.entity.Comment;
import com.BE.QuitCare.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    @Autowired
    private CommentRepository repository;

    @Autowired
    private ModelMapper mapper;

    public List<CommentDTO> getAll() {
        return repository.findAll().stream()
                .map(comment -> mapper.map(comment, CommentDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<CommentDTO> getById(Long id) {
        return repository.findById(id)
                .map(comment -> mapper.map(comment, CommentDTO.class));
    }

    public CommentDTO create(CommentDTO dto) {
        Comment comment = mapper.map(dto, Comment.class);
        return mapper.map(repository.save(comment), CommentDTO.class);
    }

    public CommentDTO update(Long id, CommentDTO dto) {
        return repository.findById(id).map(comment -> {
            comment.setContent(dto.getContent());
            comment.setCommentStatus(dto.getCommentStatus());
            comment.setCreateAt(dto.getCreateAt());
            return mapper.map(repository.save(comment), CommentDTO.class);
        }).orElse(null);
    }

    public boolean softDelete(Long id) {
        return repository.findById(id).map(comment -> {
            repository.delete(comment); // hoặc đặt một flag nếu bạn dùng soft delete
            return true;
        }).orElse(false);
    }
}
