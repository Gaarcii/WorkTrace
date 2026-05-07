package com.worktrace.worktracebackend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    private MultipartFile avatar;
    private String email;
    private String phone;
    private String actualPassword;
    private Boolean deleteAvatar;
}
