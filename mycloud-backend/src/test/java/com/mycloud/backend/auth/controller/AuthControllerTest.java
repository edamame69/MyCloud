package com.mycloud.backend.auth.controller;

import com.mycloud.backend.auth.exception.AuthExceptionHandler;
import com.mycloud.backend.auth.exception.EmailAlreadyExistsException;
import com.mycloud.backend.auth.exception.InvalidCredentialsException;
import com.mycloud.backend.auth.dto.LoginResponse;
import com.mycloud.backend.auth.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests the HTTP boundary with a mocked service; does not prove registration persistence.
class AuthControllerTest {
    private final AuthService service = mock(AuthService.class, withSettings().mockMaker(MockMakers.SUBCLASS));
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service))
                .setControllerAdvice(new AuthExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void closeValidator() { validator.close(); }

    @Test
    void returnsOnlySuccessMessageAndDelegatesNormalizedValues() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":" Minh@example.com ","firstName":" Minh ","lastName":" Lê ","password":"zqxjkvbn"}
                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"message\":\"Tạo tài khoản thành công\"}"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        verify(service).register("Minh@example.com", "Minh", "Lê", "zqxjkvbn");
    }

    @Test
    void mapsEmailConflictTo409() throws Exception {
        doThrow(new EmailAlreadyExistsException()).when(service).register(anyString(), anyString(), anyString(), anyString());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"minh@example.com","firstName":"Minh","lastName":"Lê","password":"zqxjkvbn"}
                """))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"message\":\"Email đã tồn tại\"}"));
    }

    @Test
    void rejectsInvalidOrMalformedRequestsBeforeCallingService() throws Exception {
        for (String body : new String[]{"""
                {"email":"wrong","firstName":"Minh","lastName":"Lê","password":"12345678"}
                """, "{}", "{broken"}) {
            mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("{\"message\":\"Dữ liệu đăng ký không hợp lệ\"}"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void returnsLoginTokenWithoutCachingAndDelegatesNormalizedInput() throws Exception {
        when(service.login("Minh@example.com", " café cloud "))
                .thenReturn(new LoginResponse("Đăng nhập thành công", "test-token", "Bearer", 1800));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":" Minh@example.com ","password":" café cloud "}
                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.accessToken").value("test-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.password").doesNotExist());
        verify(service).login("Minh@example.com", " café cloud ");
    }

    @Test
    void mapsInvalidCredentialsToGeneric401() throws Exception {
        when(service.login(anyString(), anyString())).thenThrow(new InvalidCredentialsException());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"minh@example.com","password":"wrong"}
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().json("{\"message\":\"Email hoặc mật khẩu không đúng\"}"));
    }

    @Test
    void rejectsInvalidLoginBeforeCallingServiceWithoutEchoingPassword() throws Exception {
        for (String body : new String[]{"{}", "{broken", """
                {"email":"invalid","password":"sensitive-password"}
                """}) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json("{\"message\":\"Dữ liệu đăng nhập không hợp lệ\"}"));
        }
        verifyNoInteractions(service);
    }
}
