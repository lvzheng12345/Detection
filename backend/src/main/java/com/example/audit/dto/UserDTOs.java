package com.example.audit.dto;

import lombok.Data;

public class UserDTOs {
    @Data
    public static class UpdateProfileRequest {
        private Long userId;
        private String email;
        private String phone;
    }

    @Data
    public static class ChangePasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;
    }
}
