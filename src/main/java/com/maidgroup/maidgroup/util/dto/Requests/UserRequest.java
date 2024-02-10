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
    @NotNull
    @NotBlank
    private String username;
    private String password;
    private String rawPassword;
    private String confirmPassword;
    @NotNull
    @NotBlank
    private String firstName;
    @NotNull
    @NotBlank
    private String lastName;
    @Email
    @NotNull
    @NotBlank
    private String email;
    @NotNull
    private Gender gender;
    @NotNull
    private LocalDate dateOfBirth;
    private Role role;
    @NotNull
    @Min(value = 0)
    private int age;

    private String jwt;
}

