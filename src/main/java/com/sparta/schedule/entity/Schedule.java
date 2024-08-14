package com.sparta.schedule.entity;


import com.sparta.schedule.dto.ScheduleRequestDto;
import com.sparta.schedule.dto.ScheduleResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Schedule {
    private String work;
    private String teacher;
    private Integer password;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Schedule(ScheduleRequestDto requestDto) {
        this.work = requestDto.getWork();
        this.teacher = requestDto.getTeacher();
        this.password = requestDto.getPassword();
    }

    public void update(ScheduleRequestDto requestDto){
        this.work = requestDto.getWork();
        this.teacher = requestDto.getTeacher();
        this.password = requestDto.getPassword();
    }
}

