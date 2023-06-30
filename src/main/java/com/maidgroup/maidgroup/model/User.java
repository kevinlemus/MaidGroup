package com.maidgroup.maidgroup.model;

import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.security.PasswordConverter;
import com.maidgroup.maidgroup.security.PasswordEmbeddable;
import com.maidgroup.maidgroup.util.dto.Requests.UserRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(name = "username", columnDefinition = "varchar(255) DEFAULT ''")
    private String username;
    @Embedded
    @Column(name = "hashed_password", columnDefinition = "varchar(255)")
    private Password password;
    @Transient
    private Password confirmPassword;
    @Transient
    private String rawPassword;
    @ElementCollection
    @CollectionTable(name = "previous_passwords", joinColumns = @JoinColumn(name = "user_id"))
    private List<Password> previousPasswords = new ArrayList<>();
    private String firstName;
    private String lastName;
    private String email;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consultation> consultations = new ArrayList<>();
    private int age;

}
