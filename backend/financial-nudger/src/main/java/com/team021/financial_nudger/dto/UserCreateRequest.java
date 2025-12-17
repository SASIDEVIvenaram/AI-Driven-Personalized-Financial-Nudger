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
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  private String password;

  @NotBlank @Size(max = 100)
  private String firstName;

  @NotBlank @Size(max = 100)
  private String lastName;

  @Size(max = 3)
  @Builder.Default
  private String currencyPreference = "INR";
}