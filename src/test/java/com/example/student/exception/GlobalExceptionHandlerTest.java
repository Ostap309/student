package com.example.student.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void catchEntityNotFoundException_shouldReturn404WithMessage() {
        EntityNotFoundException exception =
                new EntityNotFoundException("Студент с id 42 не найден");

        ResponseEntity<AppError> response = handler.catchEntityNotFoundException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getMessage()).isEqualTo("Студент с id 42 не найден");
    }

    @Test
    void catchEmailAlreadyExistsException_shouldReturn409WithMessage() {
        EmailAlreadyExistsException exception =
                new EmailAlreadyExistsException("Пользователь с таким email уже существует");

        ResponseEntity<AppError> response = handler.catchEmailAlreadyExistsException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatusCode()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().getMessage()).isEqualTo("Пользователь с таким email уже существует");
    }
}
