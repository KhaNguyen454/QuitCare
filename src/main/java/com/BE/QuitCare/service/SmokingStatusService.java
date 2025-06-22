package com.BE.QuitCare.service;


import com.BE.QuitCare.dto.SmokingStatusDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.SmokingStatus;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.SmokingStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmokingStatusService
{
    @Autowired
    private SmokingStatusRepository smokingStatusRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<SmokingStatusDTO> getAll() {
        return smokingStatusRepository.findAll()
                .stream()
                .map(smokingStatus -> modelMapper.map(smokingStatus, SmokingStatusDTO.class))
                .collect(Collectors.toList());
    }

    public SmokingStatusDTO getById(Long id) {
        return smokingStatusRepository.findById(id)
                .map(smokingStatus -> modelMapper.map(smokingStatus, SmokingStatusDTO.class))
                .orElse(null);
    }

    public SmokingStatusDTO create(Long accountId, SmokingStatusDTO dto) {
        Account account = authenticationRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        // Kiểm tra account đã có SmokingStatus chưa
        if (smokingStatusRepository.findByAccountId(accountId).isPresent()) {
            throw new IllegalStateException("Account already has a SmokingStatus.");
        }

        // Map DTO → entity
        SmokingStatus entity = modelMapper.map(dto, SmokingStatus.class);
        entity.setAccount(account); // Gắn với account

        SmokingStatus saved = smokingStatusRepository.save(entity);
        return modelMapper.map(saved, SmokingStatusDTO.class);
    }


    public SmokingStatusDTO update(Long id, SmokingStatusDTO dto) {
        return smokingStatusRepository.findById(id)
                .map(existing -> {
                    modelMapper.map(dto, existing); // copy fields from dto to entity
                    return modelMapper.map(smokingStatusRepository.save(existing), SmokingStatusDTO.class);
                })
                .orElse(null);
    }

    public boolean delete(Long id) {
        if (!smokingStatusRepository.existsById(id)) return false;
        smokingStatusRepository.deleteById(id);
        return true;
    }
}
