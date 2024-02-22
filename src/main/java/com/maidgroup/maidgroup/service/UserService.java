package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.userinfo.Gender;
import com.maidgroup.maidgroup.util.dto.Requests.ForgotPasswordRequest;
import com.maidgroup.maidgroup.util.dto.Requests.ResetPasswordRequest;
import com.maidgroup.maidgroup.util.dto.Responses.UserResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    User login(String username, String password);
    void logout(String token);
    User register(User user);
    User updateUser(User user);
    List<User> getAllUsers(User requester, String sort, String firstName, String lastName, Gender gender, String email);
    User getByUsername(String username, User requester);
    void delete(Long userId, User requester);
    void deactivateAccount(Long userId, User requester);
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);

}
