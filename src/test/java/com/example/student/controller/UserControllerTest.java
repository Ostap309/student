package com.example.student.controller;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.security.JwtService;
import com.example.student.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        UserResponseList body = new UserResponseList(
                List.of(new UserResponse(1L, "admin@mail.ru", java.util.Set.of())), 1);
        when(userService.findAll()).thenReturn(body);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.responses[0].email").value("admin@mail.ru"));
    }

    @Test
    void updateUser_shouldDelegateToService() throws Exception {
        when(userService.updateUser(any(UserRequest.class)))
                .thenReturn(new UserResponse(1L, "admin@mail.ru", java.util.Set.of()));

        mockMvc.perform(put("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@mail.ru","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService).updateUser(any(UserRequest.class));
    }

    @Test
    void save_shouldDelegateToService() throws Exception {
        when(userService.save(any(UserRequest.class)))
                .thenReturn(new UserResponse(2L, "new@mail.ru", java.util.Set.of()));

        mockMvc.perform(post("/api/v1/users/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@mail.ru","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(userService).save(any(UserRequest.class));
    }

    @Test
    void deleteById_shouldDelegateToService() throws Exception {
        mockMvc.perform(delete("/api/v1/users/3"))
                .andExpect(status().isOk());

        verify(userService).deleteById(3L);
    }
}
