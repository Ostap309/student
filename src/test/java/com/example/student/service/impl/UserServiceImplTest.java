package com.example.student.service.impl;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.exception.EmailAlreadyExistsException;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.UserMapper;
import com.example.student.model.Role;
import com.example.student.model.User;
import com.example.student.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest request() {
        UserRequest request = new UserRequest();
        request.setEmail("admin@mail.ru");
        request.setPassword("secret");
        request.setRoles(Set.<Role>of());
        return request;
    }

    @Test
    void existsByEmail_shouldDelegateToRepository() {
        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(true);

        assertThat(userService.existsByEmail("admin@mail.ru")).isTrue();
        assertThat(userService.existsByEmail("other@mail.ru")).isFalse();
    }

    @Test
    void register_shouldReturnFalse_whenMapperReturnsNull() {
        when(userMapper.toEntity(any())).thenReturn(null);

        assertThat(userService.register(new UserRequest())).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldReturnFalse_whenEmailIsNull() {
        UserRequest request = new UserRequest(); // email не задан
        when(userMapper.toEntity(request)).thenReturn(new User());

        assertThat(userService.register(request)).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldReturnFalse_whenEmailAlreadyExists() {
        User user = new User();
        user.setEmail("admin@mail.ru");
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(true);

        assertThat(userService.register(request())).isFalse();
        verify(userRepository, never()).save(any());
    }


    @Test
    void register_shouldEncodePasswordAndSave() {
        User user = new User();
        user.setEmail("admin@mail.ru");
        user.setPassword("secret");
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-hash");

        boolean result = userService.register(request());

        assertThat(result).isTrue();
        assertThat(user.getPassword()).isEqualTo("encoded-hash");
        verify(userRepository).save(user);
    }

    @Test
    void register_shouldNotEncode_whenPasswordBlank() {
        User user = new User();
        user.setEmail("admin@mail.ru");
        user.setPassword("   ");
        when(userMapper.toEntity(any())).thenReturn(user);
        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(false);

        boolean result = userService.register(request());

        assertThat(result).isTrue();
        assertThat(user.getPassword()).isEqualTo("   "); // остался как был
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        User user = new User();
        when(userRepository.findByEmail("admin@mail.ru")).thenReturn(Optional.of(user));

        assertThat(userService.findByEmail("admin@mail.ru")).isSameAs(user);
    }

    @Test
    void findByEmail_shouldThrow_whenNotFound() {
        when(userRepository.findByEmail("ghost@mail.ru")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("ghost@mail.ru"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with email ghost@mail.ru not found!");
    }

    @Test
    void findAll_shouldDelegateToMapper() {
        UserResponseList listResponse = new UserResponseList();
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        when(userMapper.toListResponse(any())).thenReturn(listResponse);

        assertThat(userService.findAll()).isSameAs(listResponse);
    }

    @Test
    void save_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.save(request()))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email admin@mail.ru is already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void save_shouldEncodePasswordSetRolesAndReturnResponse() {
        when(userRepository.existsByEmail("admin@mail.ru")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = new UserResponse(1L, "admin@mail.ru", Set.<Role>of());
        when(userMapper.toResponse(any(User.class))).thenReturn(response);

        UserResponse result = userService.save(request());

        assertThat(result).isSameAs(response);
        verify(userRepository).save(any(User.class));
    }


    @Test
    void updateUser_shouldApplyNonEmptyRoles() {
        User existed = new User();
        existed.setEmail("admin@mail.ru");
        UserResponse response = new UserResponse();
        when(userRepository.findByEmail("admin@mail.ru")).thenReturn(Optional.of(existed));
        when(userMapper.toResponse(existed)).thenReturn(response);

        UserRequest request = request();
        request.setRoles(EnumSet.allOf(Role.class)); // непустое множество любых ролей

        UserResponse result = userService.updateUser(request);

        assertThat(existed.getRoles()).isNotEmpty(); // роли применились
        assertThat(result).isSameAs(response);
    }

    @Test
    void updateUser_shouldKeepRoles_whenRequestRolesEmpty() {
        User existed = new User();
        existed.setEmail("admin@mail.ru");
        existed.setRoles(EnumSet.allOf(Role.class));
        when(userRepository.findByEmail("admin@mail.ru")).thenReturn(Optional.of(existed));
        when(userMapper.toResponse(existed)).thenReturn(new UserResponse());

        UserRequest request = request();
        request.setRoles(Set.of()); // пустое множество

        userService.updateUser(request);

        assertThat(existed.getRoles()).isNotEmpty(); // не затёрлись
    }

    @Test
    void updateUser_shouldPropagateNotFound() {
        UserRequest request = request();
        when(userRepository.findByEmail("admin@mail.ru")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteById_shouldDelegateToRepository() {
        userService.deleteById(5L);

        verify(userRepository).deleteById(5L);
    }
}
