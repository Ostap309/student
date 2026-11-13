package com.example.student.mapper;

import com.example.student.dto.*;
import com.example.student.model.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperImplTest {

    private final StudentMapperImpl mapper = new StudentMapperImpl();

    private Student student() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Иван");
        student.setSurname("Иванов");
        student.setPhoneNumber("+79001234567");
        student.setEmail("ivan@mail.ru");
        return student;
    }

    @Test
    void requestToStudent_shouldCopyAllFields() {
        StudentRequest request = new StudentRequest();
        request.setName("Иван");
        request.setSurname("Иванов");
        request.setPhoneNumber("+79001234567");
        request.setEmail("ivan@mail.ru");

        Student student = mapper.requestToStudent(request);

        assertThat(student.getId()).isNull(); // id назначит БД, маппер его не трогает
        assertThat(student.getName()).isEqualTo("Иван");
        assertThat(student.getSurname()).isEqualTo("Иванов");
        assertThat(student.getPhoneNumber()).isEqualTo("+79001234567");
        assertThat(student.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void studentToFullResponse_shouldCopyAllFields() {
        StudentFullResponse response = mapper.studentToFullResponse(student());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Иван");
        assertThat(response.getSurname()).isEqualTo("Иванов");
        assertThat(response.getPhoneNumber()).isEqualTo("+79001234567");
        assertThat(response.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void studentToResponse_shouldCopyThreeFields() {
        StudentResponse response = mapper.studentToResponse(student());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Иван");
        assertThat(response.getSurname()).isEqualTo("Иванов");
    }

    @Test
    void studentToStudentResponseForList_shouldCopyThreeFields() {
        StudentResponseForList response = mapper.studentToStudentResponseForList(student());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Иван");
        assertThat(response.getSurname()).isEqualTo("Иванов");
    }

    @Test
    void studentListToListOfStudentResponse_shouldMapEveryElement() {
        Student second = student();
        second.setId(2L);

        List<StudentResponseForList> list = mapper.studentListToListOfStudentResponse(
                List.of(student(), second));

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void studentListToStudentResponseList_shouldFillListAndCount() {
        StudentResponseList result = mapper.studentListToStudentResponseList(
                List.of(student(), student()));

        assertThat(result.getResponse()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
    }

    @Test
    void studentListToStudentResponseList_shouldReturnEmptyOnEmptyInput() {
        StudentResponseList result = mapper.studentListToStudentResponseList(List.of());

        assertThat(result.getResponse()).isEmpty();
        assertThat(result.getTotalCount()).isZero();
    }
}
