package com.example.student.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentResponseList {
    List<StudentResponseForList> response;
    int totalCount;
}
