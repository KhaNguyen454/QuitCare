package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.CommentDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Comment;
import com.BE.QuitCare.entity.CommunityPost;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.CommentRepository;
import com.BE.QuitCare.repository.CommunityPostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    public List<CommentDTO> getAll() {
        return repository.findAll().stream()
                .map(comment -> mapper.map(comment, CommentDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<CommentDTO> getById(Long id) {
        return repository.findById(id)
                .map(comment -> mapper.map(comment, CommentDTO.class));
    }

//    public CommentDTO create(CommentDTO dto) {
//        Comment comment = mapper.map(dto, Comment.class);
//        return mapper.map(repository.save(comment), CommentDTO.class);
//    }

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

    public List<CommentDTO> getCommentsByPostId(Long communityPostId) {
        // Đảm bảo bài post tồn tại trước khi lấy comment
        communityPostRepository.findById(communityPostId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết với ID: " + communityPostId));

        return repository.findByCommunityPostId(communityPostId).stream()
                .map(comment -> mapper.map(comment, CommentDTO.class))
                .collect(Collectors.toList());
    }

    public CommentDTO createCommentForPostAndAccount(Long communityPostId, Long accountId, CommentDTO dto) {
        CommunityPost post = communityPostRepository.findById(communityPostId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết với ID: " + communityPostId));
        Account account = authenticationRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + accountId));

        Comment comment = mapper.map(dto, Comment.class);
        comment.setCommunityPost(post);
        comment.setAccount(account);
        comment.setCreateAt(LocalDateTime.now()); // Thiết lập thời gian tạo
        // Mặc định trạng thái comment nếu cần, ví dụ: comment.setCommentStatus(CommentStatus.ACTIVE);

        Comment savedComment = repository.save(comment);
        // Cập nhật DTO với các trường từ Entity đã lưu
        CommentDTO savedDto = mapper.map(savedComment, CommentDTO.class);
        savedDto.setCommunityPostId(savedComment.getCommunityPost().getId());
        savedDto.setAccountId(savedComment.getAccount().getId());
        return savedDto;
    }

    public CommentDTO updateCommentForPostAndAccount(Long communityPostId, Long commentId, Long accountId, CommentDTO dto) {
        // Tìm comment theo ID và đảm bảo nó thuộc về người dùng và bài post đúng
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        if (!comment.getCommunityPost().getId().equals(communityPostId)) {
            throw new IllegalArgumentException("Bình luận không thuộc về bài viết này.");
        }
        if (!comment.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền cập nhật bình luận này.");
        }

        comment.setContent(dto.getContent());
        // Bạn có thể cho phép cập nhật status hoặc không tùy theo logic nghiệp vụ
        // comment.setCommentStatus(dto.getCommentStatus());

        Comment updatedComment = repository.save(comment);
        CommentDTO updatedDto = mapper.map(updatedComment, CommentDTO.class);
        updatedDto.setCommunityPostId(updatedComment.getCommunityPost().getId());
        updatedDto.setAccountId(updatedComment.getAccount().getId());
        return updatedDto;
    }

    public boolean deleteCommentForPostAndAccount(Long communityPostId, Long commentId, Long accountId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        if (!comment.getCommunityPost().getId().equals(communityPostId)) {
            throw new IllegalArgumentException("Bình luận không thuộc về bài viết này.");
        }
        if (!comment.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền xóa bình luận này.");
        }

        // Thực hiện xóa mềm (ví dụ: đặt cờ deleted = true)
        // Nếu bạn muốn xóa hẳn khỏi DB thì dùng repository.delete(comment);
        // Vì entity Comment không có trường `deleted`, tôi sẽ dùng delete()
        repository.delete(comment);
        return true;
    }

}
