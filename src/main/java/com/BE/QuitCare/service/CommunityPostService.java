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
    CommunityPostRepository repository;

    @Autowired
    ModelMapper modelMapper;

    // (Tùy chọn) Nếu bạn muốn CommunityPostDTO bao gồm cả comment khi getById,
    // bạn cần inject CommentService và cập nhật phương thức getById
    // @Autowired
    // private CommentService commentService;

    // Các hàm đã có (không sửa đổi)
    public List<CommunityPostDTO> getAll() {
        return repository.findAll().stream()
                .map(post -> modelMapper.map(post, CommunityPostDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<CommunityPostDTO> getById(Long id) {
        return repository.findById(id)
                .map(post -> {
                    CommunityPostDTO dto = modelMapper.map(post, CommunityPostDTO.class);
                    // (Tùy chọn) Nếu muốn, bạn có thể lấy và thêm comment vào DTO ở đây
                    // if (commentService != null) {
                    //     dto.setComments(commentService.getCommentsByPostId(id));
                    // }
                    return dto;
                });
    }

    public CommunityPostDTO create(CommunityPostDTO dto) {
        CommunityPost post = new CommunityPost();
        post.setDescription(dto.getDescription());
        post.setTitle(dto.getTitle());
        post.setImage(dto.getImage());
        post.setStatus(dto.getStatus());
        post.setDate(dto.getDate());
        // Nếu CommunityPost có liên kết với Account, bạn cần thiết lập nó ở đây
        // post.setAccount(authenticationRepository.findById(dto.getAccountId()).orElseThrow(() -> new EntityNotFoundException("Account not found")));
        CommunityPost saved = repository.save(post);
        return modelMapper.map(saved, CommunityPostDTO.class);
    }

    public CommunityPostDTO update(Long id, CommunityPostDTO dto) {
        Optional<CommunityPost> optional = repository.findById(id);
        if (optional.isPresent()) {
            CommunityPost post = optional.get();
            // modelMapper.map(dto, post); // Có thể gây ra lỗi nếu DTO chứa trường phức tạp (ví dụ: List<CommentDTO>)
            // Thay vào đó, cập nhật thủ công các trường được phép cập nhật
            post.setTitle(dto.getTitle());
            post.setDescription(dto.getDescription());
            post.setImage(dto.getImage());
            post.setCategory(dto.getCategory());
            post.setStatus(dto.getStatus());
            post.setDate(dto.getDate());

            return modelMapper.map(repository.save(post), CommunityPostDTO.class);
        }
        return null;
    }

    public boolean delete(Long id) {
        Optional<CommunityPost> optional = repository.findById(id);
        if (optional.isPresent()) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
