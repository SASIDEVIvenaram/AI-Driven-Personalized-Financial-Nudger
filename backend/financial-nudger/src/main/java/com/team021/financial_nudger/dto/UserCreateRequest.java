package com.team021.financial_nudger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
  @Email @NotBlank
  private String email;

  @NotBlank
  private String passwordHash;

  @NotBlank
  private String salt;

  @NotBlank @Size(max = 100)
  private String firstName;

  @NotBlank @Size(max = 100)
  private String lastName;

  @Size(max = 3)
  private String currencyPreference = "INR";
}