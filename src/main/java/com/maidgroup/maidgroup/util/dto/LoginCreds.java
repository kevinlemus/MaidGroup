package com.maidgroup.maidgroup.util.dto;

import com.maidgroup.maidgroup.security.Password;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginCreds {

    private String username;
    private String password;
    private boolean rememberMe;

}