package com.example.student.mapper;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.model.Role;
import com.example.student.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperImplTest {

    private final UserMapperImpl mapper = new UserMapperImpl();

    private User user() {
        return User.builder()
                .id(1L)
                .email("admin@mail.ru")
                .password("secret")
                .roles(Set.of())
                .build();
    }

    @Test
    void toResponse_shouldCopyFieldsWithoutPassword() {
        UserResponse response = mapper.toResponse(user());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("admin@mail.ru");
        assertThat(response.getRoles()).isSameAs(user().getRoles());
    }

    @Test
    void toEntity_shouldCopyAllFields() {
        UserRequest request = new UserRequest();
        request.setEmail("admin@mail.ru");
        request.setPassword("secret");
        request.setRoles(Set.<Role>of());

        User user = mapper.toEntity(request);

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo("admin@mail.ru");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRoles()).isSameAs(request.getRoles());
    }

    @Test
    void toResponseList_shouldMapEveryUser() {
        List<UserResponse> list = mapper.toResponseList(List.of(user(), user()));

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getEmail()).isEqualTo("admin@mail.ru");
    }

    @Test
    void toListResponse_shouldFillListAndCount() {
        UserResponseList result = mapper.toListResponse(List.of(user(), user()));

        assertThat(result.getResponses()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
    }

    @Test
    void toListResponse_shouldReturnEmptyOnEmptyInput() {
        UserResponseList result = mapper.toListResponse(List.of());

        assertThat(result.getResponses()).isEmpty();
        assertThat(result.getTotalCount()).isZero();
    }
}
