package com.example.student.controller;

import com.example.student.dto.*;
import com.example.student.security.JwtService;
import com.example.student.service.StudentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void findAll_shouldReturnList() throws Exception {
        StudentResponseList body = new StudentResponseList(
                List.of(new StudentResponseForList(1L, "Иван", "Иванов")), 1);
        when(studentService.findAll()).thenReturn(body);

        mockMvc.perform(get("/api/v1/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.response[0].name").value("Иван"));
    }

    @Test
    void findStudentById_shouldReturnFullResponse() throws Exception {
        StudentFullResponse body = new StudentFullResponse(
                1L, "Иван", "Иванов", "+79001234567", "ivan@mail.ru");
        when(studentService.findById(1L)).thenReturn(body);

        mockMvc.perform(get("/api/v1/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void getFullInfoById_shouldReturnFullResponse() throws Exception {
        StudentFullResponse body = new StudentFullResponse(
                2L, "Пётр", "Петров", "+79007654321", "petr@mail.ru");
        when(studentService.findById(2L)).thenReturn(body);

        mockMvc.perform(get("/api/v1/student/info/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void createStudent_shouldDelegateToService() throws Exception {
        StudentRequest request = new StudentRequest();
        request.setName("Иван");
        request.setSurname("Иванов");
        request.setPhoneNumber("+79001234567");
        request.setEmail("ivan@mail.ru");
        when(studentService.save(any(StudentRequest.class)))
                .thenReturn(new StudentResponse(1L, "Иван", "Иванов"));

        mockMvc.perform(post("/api/v1/student/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иван","surname":"Иванов",
                                 "phoneNumber":"+79001234567","email":"ivan@mail.ru"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(studentService).save(any(StudentRequest.class));
    }

    @Test
    void updateStudent_shouldDelegateToService() throws Exception {
        StudentUpdateRequest request = new StudentUpdateRequest();
        request.setPhoneNumber("+79009999999");
        request.setEmail("new@mail.ru");
        when(studentService.update(eq(1L), any(StudentUpdateRequest.class)))
                .thenReturn(new StudentResponse(1L, "Иван", "Иванов"));

        mockMvc.perform(put("/api/v1/student/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phoneNumber":"+79009999999","email":"new@mail.ru"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(studentService).update(eq(1L), any(StudentUpdateRequest.class));
    }

    @Test
    void deleteStudentById_shouldDelegateToService() throws Exception {
        mockMvc.perform(delete("/api/v1/student/1"))
                .andExpect(status().isOk());

        verify(studentService).deleteById(1L);
    }
}
