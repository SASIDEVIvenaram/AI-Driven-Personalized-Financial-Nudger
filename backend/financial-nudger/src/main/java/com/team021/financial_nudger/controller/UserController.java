package com.team021.financial_nudger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team021.financial_nudger.dto.UserCreateRequest;
import com.team021.financial_nudger.dto.UserResponse;
import com.team021.financial_nudger.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  public UserController(UserService userService) { this.userService = userService; }

  @PostMapping
  public ResponseEntity<UserResponse> create(@RequestBody @Valid UserCreateRequest req) {
    return ResponseEntity.ok(userService.create(req));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> get(@PathVariable Integer id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> list() {
    return ResponseEntity.ok(userService.list());
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> update(@PathVariable Integer id,
                                             @RequestBody @Valid UserCreateRequest req) {
    return ResponseEntity.ok(userService.update(id, req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}