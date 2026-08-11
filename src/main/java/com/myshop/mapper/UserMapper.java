package com.myshop.mapper;

import com.myshop.domain.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User findByEmail(@Param("email") String email);
    int insert(User user);
}
