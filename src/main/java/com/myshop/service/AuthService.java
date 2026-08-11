package com.myshop.service;

import com.myshop.domain.SessionUser;

public interface AuthService {
    SessionUser register(String email, String password, String name);
    SessionUser authenticate(String email, String password);
}
