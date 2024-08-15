package com.sparta.schedule.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleRequestDto {
    private String work;
    private Integer teacherId;
    private Integer password;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;

}
