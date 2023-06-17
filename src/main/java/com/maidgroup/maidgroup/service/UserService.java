package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    User login(String username, String password);
    void logout(HttpServletRequest request);
    User register(User user, String jsonPayload);
    User updateUser(User user);
    List<User> getAllUsers(User user);
    User getByUsername(String username, User requester);
    void delete(String username, User requester);

}
