package com.maidgroup.maidgroup.util.dto.Requests;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private Long userId;
    private String username;
    private String password;
    private String rawPassword;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    @Email
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Role role;
    @Min(value = 0)
    private int age;
    private LocalDate deactivationDate;
    private String jwt;
}

