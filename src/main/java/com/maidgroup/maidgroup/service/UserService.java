package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.util.dto.Responses.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    User login(String username, String password);
    void logout(String token);
    User register(User user);
    User updateUser(User user);
    List<UserResponse> getAllUsers(User user);
    User getByUsername(String username, User requester);
    void delete(String username, User requester);

}
