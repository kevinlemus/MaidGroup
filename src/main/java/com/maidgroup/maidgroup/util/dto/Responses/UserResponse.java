package com.maidgroup.maidgroup.util.dto.Responses;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private Password password;
    private List<Password> previousPasswords = new ArrayList<>();
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Role role;
    private int age;

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.previousPasswords = user.getPreviousPasswords();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.gender = user.getGender();
        this.dateOfBirth = user.getDateOfBirth();
        this.role = user.getRole();
        this.age = user.getAge();
    }
}
