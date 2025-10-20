package com.team021.financial_nudger.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team021.financial_nudger.domain.User;
import com.team021.financial_nudger.dto.UserCreateRequest;
import com.team021.financial_nudger.dto.UserResponse;
import com.team021.financial_nudger.exception.NotFoundException;
import com.team021.financial_nudger.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

  @Transactional
  public UserResponse create(UserCreateRequest req) {
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }
    User user = new User(
        req.getEmail(),
        req.getPasswordHash(),
        req.getSalt(),
        req.getFirstName(),
        req.getLastName()
    );
    user.setCurrencyPreference(req.getCurrencyPreference());
    return toResponse(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public UserResponse getById(Integer id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found: " + id));
    return toResponse(user);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> list() {
    return userRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional
  public UserResponse update(Integer id, UserCreateRequest req) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found: " + id));
    user.setFirstName(req.getFirstName());
    user.setLastName(req.getLastName());
    user.setCurrencyPreference(req.getCurrencyPreference());
    if (req.getPasswordHash() != null && !req.getPasswordHash().isBlank()) {
      user.setPasswordHash(req.getPasswordHash());
    }
    if (req.getSalt() != null && !req.getSalt().isBlank()) {
      user.setSalt(req.getSalt());
    }
    return toResponse(userRepository.save(user));
  }

  @Transactional
  public void delete(Integer id) {
    if (!userRepository.existsById(id)) throw new NotFoundException("User not found: " + id);
    userRepository.deleteById(id);
  }

  private UserResponse toResponse(User u) {
    return UserResponse.builder()
        .userId(u.getUserId())
        .email(u.getEmail())
        .firstName(u.getFirstName())
        .lastName(u.getLastName())
        .currencyPreference(u.getCurrencyPreference())
        .build();
  }
}