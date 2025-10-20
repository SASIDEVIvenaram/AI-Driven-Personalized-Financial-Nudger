package com.team021.financial_nudger.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer userId;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false,unique = true,length = 255)
    private String email;

    @NotBlank(message = "Password hash is required")
    @Column(name="password_hash",nullable = false,length = 255)
    private String passwordHash;

    @NotBlank(message = "Salt is required")
    @Column(nullable = false,length=255)
    private String salt;

    @NotBlank(message = "First name is required")
    @Size(max=100,message = "First name must not exceed 100 characters")
    @Column(name = "first_name",nullable = false,length = 100)
    private String firstName;


    @NotBlank(message = "Last name is required")
    @Size(max=100,message = "Last name must not exceed 100 characters")
    @Column(name="last_name",nullable = false,length=100)
    private String lastName;

    @Size(max=3,message = "Currency preference must not exceed 3 characters")
    @Column(name="currency_preference",length=3)
    private String currencyPreference="INR";

    @Column(name="created_at",updatable = false,insertable = false)
    private Instant createdAt;

    // Constructors
    public User() {}
    
    public User(String email, String passwordHash, String salt, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCurrencyPreference() {
        return currencyPreference;
    }

    public void setCurrencyPreference(String currencyPreference) {
        this.currencyPreference = currencyPreference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{"+
                "userId="+userId+
                ", email='"+email+"\'"+
                ", firstName='"+firstName+"\'"+
                ", lastName='"+lastName+"\'"+
                ", currencyPreference='"+currencyPreference+"\'"+
                ", createdAt="+createdAt+
                "}";

    }
}