package com.myshop.service.impl;

import com.myshop.config.MyBatisProvider;
import com.myshop.domain.SessionUser;
import com.myshop.domain.User;
import com.myshop.mapper.UserMapper;
import com.myshop.service.AuthService;
import com.myshop.service.DuplicateEmailException;
import java.util.Locale;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.mindrot.jbcrypt.BCrypt;

public class AuthServiceImpl implements AuthService {
    private static final String DUMMY_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Override
    public SessionUser register(String email, String password, String name) {
        String normalizedEmail = normalizeEmail(email);
        try (SqlSession session = MyBatisProvider.getFactory().openSession(false)) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            if (mapper.findByEmail(normalizedEmail) != null) throw new DuplicateEmailException();
            User user = new User();
            user.setEmail(normalizedEmail);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
            user.setName(name.trim());
            user.setRole("CUSTOMER");
            try {
                mapper.insert(user);
                session.commit();
                return new SessionUser(user);
            } catch (PersistenceException exception) {
                session.rollback();
                throw new DuplicateEmailException(exception);
            }
        }
    }

    @Override
    public SessionUser authenticate(String email, String password) {
        try (SqlSession session = MyBatisProvider.getFactory().openSession()) {
            User user = session.getMapper(UserMapper.class).findByEmail(normalizeEmail(email));
            String hash = user == null ? DUMMY_HASH : user.getPasswordHash();
            boolean matches = BCrypt.checkpw(password, hash);
            return user != null && matches ? new SessionUser(user) : null;
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
