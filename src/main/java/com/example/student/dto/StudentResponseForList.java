package com.example.student.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StudentResponseForList {
    private long id;
    private String name;
    private String surname;
}
