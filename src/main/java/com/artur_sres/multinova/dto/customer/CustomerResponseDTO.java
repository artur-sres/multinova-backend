package com.artur_sres.multinova.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private LocalDate birthDate;
    private String photoUrl;
    private String preferences;
    private BigDecimal debtBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}

    public LocalDate getBirthDate() {return birthDate;}
    public void setBirthDate(LocalDate birthDate) {this.birthDate = birthDate;}

    public String getPhotoUrl() {return photoUrl;}
    public void setPhotoUrl(String photoUrl) {this.photoUrl = photoUrl;}

    public String getPreferences() {return preferences;}
    public void setPreferences(String preferences) {this.preferences = preferences;}

    public BigDecimal getDebtBalance() {return debtBalance;}
    public void setDebtBalance(BigDecimal debtBalance) {this.debtBalance = debtBalance;}

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; }

    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}
}

