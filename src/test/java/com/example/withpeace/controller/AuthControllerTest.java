package com.example.withpeace.controller;

import com.example.withpeace.constant.Constant;
import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.dto.response.LoginResponseDto;
import com.example.withpeace.service.AuthService;
import com.example.withpeace.type.EProvider;
import com.example.withpeace.type.ERole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("구글 로그인 요청 테스트")
    public void loginUsingGoogleTest() throws Exception {
        // Given
        final String accessToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZmOTc3N2E2ODU5MDc3OThlZjc5NDA2MmMwMGI2NWQ2NmMyNDBiMWIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MDk2OTc2NjU3NTYtb2llZ3U3ZHB1YzZuZXQ3dGhidDBsbG1oYjd0YzdpNzIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MDk2OTc2NjU3NTYtZXI2aWExaHJldjA1am9rN2I5b3FrYjRnMDE0N24wMmEuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDg0MjAxNzMxODYxOTMwNTk4OTciLCJlbWFpbCI6InJoa3J3bmd1ZDQ0NUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IuqzveyjvO2YlSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NKczhicW82V3N2RUZ5TXhKSDdhQ0lsZE9TSEstLXRVM1FqZXZVVzMtNlM9czk2LWMiLCJnaXZlbl9uYW1lIjoi7KO87ZiVIiwiZmFtaWx5X25hbWUiOiLqs70iLCJsb2NhbGUiOiJrbyIsImlhdCI6MTcwOTA0MDIwNywiZXhwIjoxNzA5MDQzODA3fQ.ADbm6oo9YYQ_5GLTuY3JjZb0ncWNDgg38g9lal60cky2GOri0iGovq2-2fb8LW4Q-y4W0XMvetGzB--XRNtQSf-fRW4S06h3ZZ6uAhtF7WBxmI-sn5yuRACIn5dw0qrRViVMT4oH6Pgj8do_n6fpMOFO0UhJthQ_KtjoBE39rlEfIggUUCFTFbEbocWvwJuT5co1xCSE1u8JyaH2uezD5xb0hWGDTt8MZhSOQG2rHqLRilv95SuS8QbAXtTSn3kaYXJ7kJXJELds7WPjxtE635-0Mrl_u_40_0K9EW8Jddi-HiDk6tOgOEwOsx7Iyq5bd91q_4VCqt1nlBUXmsZAKw";
        final JwtTokenDto jwtTokenDto = new JwtTokenDto("test_jwtToken", "test_refreshToken");
        LoginResponseDto loginResponseDto = new LoginResponseDto(jwtTokenDto, ERole.USER);
        given(authService.loginForMobile(accessToken, EProvider.GOOGLE)).willReturn(loginResponseDto);

        // When
        ResultActions actions = mockMvc.perform(post("/api/v1/auth/google")
                .with(csrf()) // CSRF 토큰 추가
                .with(user("testUser").roles("USER")) // 로그인한 사용자 설정
                .header(Constant.AUTHORIZATION_HEADER, Constant.BEARER_PREFIX + accessToken));

        // Then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(loginResponseDto.jwtTokenDto().getAccessToken()))
                .andExpect(jsonPath("$.data.refreshToken").value(loginResponseDto.jwtTokenDto().getRefreshToken()));
    }
}