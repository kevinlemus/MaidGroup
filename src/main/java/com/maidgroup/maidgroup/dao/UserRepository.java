package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    @Query("select u from User u where u.username = :username")
    public User findByUsername(@Param("username") String username);

}
