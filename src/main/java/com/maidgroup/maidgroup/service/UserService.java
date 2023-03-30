package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    boolean login(String username, String password, HttpServletRequest request);
    void logout(HttpServletRequest request);
    User register(User user);
    User updateUser(User user, String username);
    List<User> getAllUsers();
    User getByUsername(String username, User requester);
    void delete(String username, User requester);

}
