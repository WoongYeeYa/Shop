package com.morrowshop.service;

import com.morrowshop.domain.SessionUser;

public interface AuthService {
    SessionUser register(String email, String password, String name);
    SessionUser authenticate(String email, String password);
}
