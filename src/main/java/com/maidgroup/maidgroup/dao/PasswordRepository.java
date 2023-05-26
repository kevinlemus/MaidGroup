package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.security.Password;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<Password, Long> {
}
