package com.maidgroup.maidgroup.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalUser {

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

}
