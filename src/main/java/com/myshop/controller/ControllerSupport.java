package com.myshop.controller;

import com.myshop.domain.SessionUser;
import javax.servlet.http.HttpServletRequest;

final class ControllerSupport {
    private ControllerSupport() {}

    static String value(HttpServletRequest request, String name) {
        return rawValue(request, name).trim();
    }

    static String rawValue(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value;
    }

    static SessionUser user(HttpServletRequest request) {
        return (SessionUser) request.getSession().getAttribute("loginUser");
    }
}
