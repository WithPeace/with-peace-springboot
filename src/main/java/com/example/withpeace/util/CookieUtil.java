package com.example.withpeace.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public class CookieUtil {
    public static Optional<Cookie> getCookie(){
        return Optional.empty();
    }
    public static void addCookie(HttpServletResponse response, String name, String value){
        Cookie cookie = new Cookie(name,value);
        // 전역 사용
        cookie.setPath("/");;

        response.addCookie(cookie);
    }

    public static void addSecureCookie(HttpServletResponse response,String name, String value, Integer maxAge){
        Cookie cookie = new Cookie(name,value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
