package com.myshop.domain;
import static org.junit.jupiter.api.Assertions.*;import org.junit.jupiter.api.Test;
class SessionUserTest { @Test void exposesRoleWithoutPasswordHash(){User user=new User();user.setId(1L);user.setEmail("admin@myshop.test");user.setName("관리자");user.setRole("ADMIN");user.setPasswordHash("secret");SessionUser session=new SessionUser(user);assertTrue(session.isAdmin());assertEquals("관리자",session.getName());} }
