package com.sparta.schedule.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleRequestDto {
    private String work;
    private String teacher;
    private Integer password;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
