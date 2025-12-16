package com.example.audit.controller;

import com.example.audit.dto.LoginRequest;
import com.example.audit.dto.UserDTOs;
import com.example.audit.entity.User;
import com.example.audit.repository.UserRepository;
import com.example.audit.repository.AppConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppConfigRepository appConfigRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check Maintenance Mode
            if ("USER".equals(user.getRole())) {
                String maintenanceMode = appConfigRepository.findByConfigKey("maintenance_mode")
                        .map(config -> config.getConfigValue())
                        .orElse("false");
                if ("true".equals(maintenanceMode)) {
                    return ResponseEntity.status(503).body("系统正在维护中，请稍后再试");
                }
            }

            if (user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.badRequest().body("用户名或密码错误");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        // Check Registration Open
        String registrationOpen = appConfigRepository.findByConfigKey("registration_open")
                .map(config -> config.getConfigValue())
                .orElse("true");
        if ("false".equals(registrationOpen)) {
            return ResponseEntity.badRequest().body("当前暂不开放注册");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole("USER"); // Default role
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件为空");
        }

        try {
            // Simple file storage (in a real app, use S3 or similar)
            String uploadDir = "uploads/avatars/";
            Files.createDirectories(Paths.get(uploadDir));
            
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + filename);
            Files.write(path, file.getBytes());
            
            String avatarUrl = "/uploads/avatars/" + filename; // Relative URL
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
                return ResponseEntity.ok(avatarUrl);
            }
            return ResponseEntity.badRequest().body("用户不存在");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("上传失败");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTOs.UpdateProfileRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            userRepository.save(user);
            return ResponseEntity.ok("个人资料更新成功");
        }
        return ResponseEntity.badRequest().body("用户不存在");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody UserDTOs.ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getPassword().equals(request.getOldPassword())) {
                return ResponseEntity.badRequest().body("旧密码错误");
            }
            user.setPassword(request.getNewPassword());
            userRepository.save(user);
            return ResponseEntity.ok("密码修改成功");
        }
        return ResponseEntity.badRequest().body("用户不存在");
    }
}
