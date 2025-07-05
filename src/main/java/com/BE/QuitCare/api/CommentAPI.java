package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.CommentDTO;
import com.BE.QuitCare.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "12.Bình luận của bài post"
)
@RestController
@RequestMapping("/api/comments")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class CommentAPI {
    @Autowired
    private CommentService service;



    @GetMapping("/comments")
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id, @RequestBody CommentDTO dto) {
        CommentDTO updated = service.update(id, dto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        if (service.softDelete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Các điểm cuối API mới theo yêu cầu ---

    /**
     * Lấy tất cả các bình luận của một bài post cụ thể.
     * URL: GET /api/community-posts/{communityPostId}/comments
     * @param communityPostId ID của bài post.
     * @return Danh sách CommentDTO.
     */
    @GetMapping("/community-posts/{communityPostId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long communityPostId) {
        List<CommentDTO> comments = service.getCommentsByPostId(communityPostId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Tạo một bình luận mới cho một bài post cụ thể bởi một người dùng cụ thể.
     * URL: POST /api/community-posts/{communityPostId}/comments/by-account/{accountId}
     * (Hoặc chỉ /comments nếu accountId được lấy từ SecurityContext)
     * @param communityPostId ID của bài post.
     * @param accountId ID của tài khoản người dùng (trong thực tế, sẽ lấy từ SecurityContext).
     * @param dto Dữ liệu comment để tạo.
     * @return CommentDTO đã tạo.
     */
    @PostMapping("/community-posts/{communityPostId}/comments/by-account/{accountId}")
    public ResponseEntity<CommentDTO> createCommentForPostAndAccount(
            @PathVariable Long communityPostId,
            @PathVariable Long accountId,
            @RequestBody CommentDTO dto) {
        CommentDTO createdComment = service.createCommentForPostAndAccount(communityPostId, accountId, dto);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    /**
     * Cập nhật một bình luận cụ thể của một người dùng trên một bài post.
     * URL: PUT /api/community-posts/{communityPostId}/comments/{commentId}/by-account/{accountId}
     * @param communityPostId ID của bài post.
     * @param commentId ID của bình luận cần cập nhật.
     * @param accountId ID của tài khoản người dùng sở hữu bình luận.
     * @param dto Dữ liệu cập nhật.
     * @return CommentDTO đã cập nhật.
     */
    @PutMapping("/community-posts/{communityPostId}/comments/{commentId}/by-account/{accountId}")
    public ResponseEntity<CommentDTO> updateCommentForPostAndAccount(
            @PathVariable Long communityPostId,
            @PathVariable Long commentId,
            @PathVariable Long accountId,
            @RequestBody CommentDTO dto) {
        CommentDTO updatedComment = service.updateCommentForPostAndAccount(communityPostId, commentId, accountId, dto);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * Xóa một bình luận cụ thể của một người dùng trên một bài post.
     * URL: DELETE /api/community-posts/{communityPostId}/comments/{commentId}/by-account/{accountId}
     * @param communityPostId ID của bài post.
     * @param commentId ID của bình luận cần xóa.
     * @param accountId ID của tài khoản người dùng sở hữu bình luận.
     * @return ResponseEntity không có nội dung.
     */
    @DeleteMapping("/community-posts/{communityPostId}/comments/{commentId}/by-account/{accountId}")
    public ResponseEntity<Void> deleteCommentForPostAndAccount(
            @PathVariable Long communityPostId,
            @PathVariable Long commentId,
            @PathVariable Long accountId) {
        if (service.deleteCommentForPostAndAccount(communityPostId, commentId, accountId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build(); // Hoặc ResponseEntity.status(HttpStatus.FORBIDDEN).build() nếu do quyền
    }
}
