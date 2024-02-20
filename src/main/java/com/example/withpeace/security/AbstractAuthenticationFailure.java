package com.example.withpeace.security;
import com.example.withpeace.dto.ExceptionDto;
import com.example.withpeace.exception.ErrorCode;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONValue;

import java.util.HashMap;
import java.util.Map;
public abstract class AbstractAuthenticationFailure {
    protected void setErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode) throws IOException, java.io.IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("data", null);
        result.put("error", new ExceptionDto(errorCode, errorCode.getMessage()));

        response.getWriter().write(JSONValue.toJSONString(result));
    }
}
