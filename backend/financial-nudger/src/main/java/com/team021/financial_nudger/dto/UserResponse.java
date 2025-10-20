package com.team021.financial_nudger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
  private Integer userId;
  private String email;
  private String firstName;
  private String lastName;
  private String currencyPreference;
}