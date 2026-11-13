package com.example.student.service.impl;

import com.example.student.dto.*;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.StudentMapper;
import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void save_shouldMapRequestSaveAndReturnResponse() {
        StudentRequest request = new StudentRequest();
        request.setName("Иван");
        request.setSurname("Иванов");

        Student student = new Student();
        StudentResponse response = new StudentResponse(1L, "Иван", "Иванов");

        when(studentMapper.requestToStudent(request)).thenReturn(student);
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.studentToResponse(student)).thenReturn(response);

        StudentResponse result = studentService.save(request);

        assertThat(result).isSameAs(response);
        verify(studentRepository).save(student);
    }

    @Test
    void findById_shouldReturnFullResponse_whenStudentExists() {
        Student student = new Student();
        StudentFullResponse fullResponse = new StudentFullResponse(
                1L, "Иван", "Иванов", "+79001234567", "ivan@mail.ru");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentMapper.studentToFullResponse(student)).thenReturn(fullResponse);

        StudentFullResponse result = studentService.findById(1L);

        assertThat(result).isSameAs(fullResponse);
    }

    @Test
    void findById_shouldThrow_whenStudentNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student with ID 99 not found!");

        verify(studentRepository).findById(99L);
    }

    @Test
    void findAll_shouldDelegateToMapperAndRepository() {
        StudentResponseList listResponse = new StudentResponseList();

        when(studentRepository.findAll()).thenReturn(List.of(new Student(), new Student()));
        when(studentMapper.studentListToStudentResponseList(
                org.mockito.ArgumentMatchers.anyList())).thenReturn(listResponse);

        StudentResponseList result = studentService.findAll();

        assertThat(result).isSameAs(listResponse);
    }

    @Test
    void update_shouldSetBothFields_whenProvided() {
        StudentUpdateRequest request = new StudentUpdateRequest();
        request.setPhoneNumber("+79001234567");
        request.setEmail("new@mail.ru");

        Student student = new Student();
        StudentResponse response = new StudentResponse(1L, "Иван", "Иванов");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.studentToResponse(student)).thenReturn(response);

        StudentResponse result = studentService.update(1L, request);

        assertThat(student.getPhoneNumber()).isEqualTo("+79001234567");
        assertThat(student.getEmail()).isEqualTo("new@mail.ru");
        assertThat(result).isSameAs(response);
    }

    @Test
    void update_shouldNotOverwriteFieldsWithNull() {
        StudentUpdateRequest request = new StudentUpdateRequest();
        request.setEmail("new@mail.ru");
        // phoneNumber остаётся null — частичное обновление

        Student student = new Student();
        student.setPhoneNumber("+79009999999");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.studentToResponse(student)).thenReturn(new StudentResponse());

        studentService.update(1L, request);

        assertThat(student.getEmail()).isEqualTo("new@mail.ru");
        assertThat(student.getPhoneNumber()).isEqualTo("+79009999999"); // не затёрт
    }

    @Test
    void deleteById_shouldDelegateToRepository() {
        studentService.deleteById(1L);

        verify(studentRepository).deleteById(1L);
    }
}
