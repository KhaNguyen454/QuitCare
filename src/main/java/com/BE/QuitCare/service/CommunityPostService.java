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

    @Autowired
    private ModelMapper modelMapper;

    public List<CommunityPostDTO> getAll() {
        return repository.findAll().stream()
                .map(post -> modelMapper.map(post, CommunityPostDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<CommunityPostDTO> getById(Long id) {
        return repository.findById(id)
                .map(post -> modelMapper.map(post, CommunityPostDTO.class));
    }

    public CommunityPostDTO create(CommunityPostDTO dto) {
        CommunityPost post = modelMapper.map(dto, CommunityPost.class);
        return modelMapper.map(repository.save(post), CommunityPostDTO.class);
    }

    public CommunityPostDTO update(Long id, CommunityPostDTO dto) {
        Optional<CommunityPost> optional = repository.findById(id);
        if (optional.isPresent()) {
            CommunityPost post = optional.get();
            modelMapper.map(dto, post); // Cập nhật thông tin từ DTO
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
