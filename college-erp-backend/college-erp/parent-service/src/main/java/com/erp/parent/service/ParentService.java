package com.erp.parent.service;

import com.erp.parent.dto.ParentDto;
import com.erp.parent.entity.Parent;
import com.erp.parent.exception.ParentException;
import com.erp.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class ParentService {

    private final ParentRepository parentRepository;

    public ParentDto.Response createParent(ParentDto.CreateRequest req) {
        if (parentRepository.existsByEmail(req.getEmail()))
            throw new ParentException("Email already registered");

        Parent p = Parent.builder()
                .firstName(req.getFirstName()).lastName(req.getLastName()).email(req.getEmail())
                .phone(req.getPhone()).alternatePhone(req.getAlternatePhone()).address(req.getAddress())
                .occupation(req.getOccupation()).relationToStudent(req.getRelationToStudent())
                .receiveSms(req.isReceiveSms()).receiveEmail(req.isReceiveEmail()).build();
        return toResponse(parentRepository.save(p));
    }

    @Transactional(readOnly = true)
    public ParentDto.Response getById(Long id) {
        return parentRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ParentException("Parent not found: " + id));
    }

    public ParentDto.Response update(Long id, ParentDto.CreateRequest req) {
        Parent p = parentRepository.findById(id)
                .orElseThrow(() -> new ParentException("Parent not found: " + id));
        if (req.getPhone() != null) p.setPhone(req.getPhone());
        if (req.getAddress() != null) p.setAddress(req.getAddress());
        if (req.getOccupation() != null) p.setOccupation(req.getOccupation());
        p.setReceiveSms(req.isReceiveSms());
        p.setReceiveEmail(req.isReceiveEmail());
        return toResponse(parentRepository.save(p));
    }

    private ParentDto.Response toResponse(Parent p) {
        return ParentDto.Response.builder()
                .id(p.getId()).firstName(p.getFirstName()).lastName(p.getLastName())
                .fullName(p.getFullName()).email(p.getEmail()).phone(p.getPhone())
                .alternatePhone(p.getAlternatePhone()).address(p.getAddress())
                .occupation(p.getOccupation()).relationToStudent(p.getRelationToStudent())
                .isActive(p.getIsActive()).receiveSms(p.getReceiveSms()).receiveEmail(p.getReceiveEmail())
                .createdAt(p.getCreatedAt()).build();
    }
}
