package com.example.student.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtFilter jwtFilter;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    private final User userDetails = new User("admin@mail.ru", "secret",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withoutAuthHeader_shouldJustContinueChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/student");

        jwtFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void withNonBearerHeader_shouldJustContinueChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/student");
        request.addHeader("Authorization", "Basic abc123");

        jwtFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void withValidToken_shouldSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/student");
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn("admin@mail.ru");
        when(userDetailsService.loadUserByUsername("admin@mail.ru")).thenReturn(userDetails);
        when(jwtService.validateToken("valid-token", userDetails)).thenReturn(true);

        jwtFilter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(userDetails);
        verify(chain).doFilter(request, response);
    }

    @Test
    void withBrokenToken_onApiPath_shouldReturn401AndStopChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/student");
        request.addHeader("Authorization", "Bearer broken-token");
        when(jwtService.extractUsername("broken-token"))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("bad token"));

        jwtFilter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("MalformedJwtException");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void withBrokenToken_onNonApiPath_shouldJustContinueChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/students.html");
        request.addHeader("Authorization", "Bearer broken-token");
        when(jwtService.extractUsername("broken-token"))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("bad token"));

        jwtFilter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
    }
}
