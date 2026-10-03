package com.piiscan.auth.service;

import com.piiscan.auth.dto.AuthResponse;
import com.piiscan.auth.dto.LoginRequest;
import com.piiscan.auth.dto.RegisterRequest;
import com.piiscan.auth.dto.UserDto;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    UserDto register(RegisterRequest registerRequest);
    UserDto getCurrentUser(String usernameOrEmail);
}
