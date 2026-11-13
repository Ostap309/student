package com.example.student.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StudentFullResponse {
    private long id;
    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
}
