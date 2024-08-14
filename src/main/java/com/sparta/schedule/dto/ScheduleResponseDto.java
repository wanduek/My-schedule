package com.sparta.schedule.dto;

import com.sparta.schedule.entity.Schedule;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ScheduleResponseDto {
    private String work;
    private String teacher;
    private Integer password;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;

    public ScheduleResponseDto(Schedule schedule){
        this.work = schedule.getWork();
        this.teacher = schedule.getTeacher();
        this.password = schedule.getPassword();
    }

    public ScheduleResponseDto(String work, String teacher, Integer password) {
        this.work = work;
        this.teacher = teacher;
        this.password = password;
        this.createdDatetime = createdDatetime;
        this.updatedDatetime = updatedDatetime;
    }
}
