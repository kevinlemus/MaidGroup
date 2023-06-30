package com.maidgroup.maidgroup.util.dto.Requests;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String username;
    private String password;
    private String rawPassword;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Role role;
    private int age;
    private String jwt;
}

