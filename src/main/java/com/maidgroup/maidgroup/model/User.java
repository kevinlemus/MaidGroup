package com.maidgroup.maidgroup.model;

import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.security.Password;
import com.maidgroup.maidgroup.security.PasswordConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    @Column(name = "username", columnDefinition = "varchar(255) DEFAULT ''")
    private String username;
    @Column(name = "password", columnDefinition = "varchar(255)")
    @Convert(converter = PasswordConverter.class, attributeName = "hashedPassword")
    private Password password;

    @Column(name = "confirmPassword", columnDefinition = "varchar(255)")
    @Convert(converter = PasswordConverter.class, attributeName = "hashedPassword")
    private Password confirmPassword;
    //@ElementCollection
    @CollectionTable(name = "previous_passwords", joinColumns = @JoinColumn(name = "username"))
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


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public Password getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(Password confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public List<Password> getPreviousPasswords() {
        return previousPasswords;
    }

    public void setPreviousPasswords(List<Password> previousPasswords) {
        this.previousPasswords = previousPasswords;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<Consultation> getConsultations() {
        return consultations;
    }

    public void setConsultations(List<Consultation> consultations) {
        this.consultations = consultations;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
