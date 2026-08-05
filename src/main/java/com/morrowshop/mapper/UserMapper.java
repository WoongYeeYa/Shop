package com.morrowshop.mapper;

import com.morrowshop.domain.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User findByEmail(@Param("email") String email);
    int insert(User user);
}
