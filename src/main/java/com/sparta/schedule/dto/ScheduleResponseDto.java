package com.sparta.schedule.dto;

import com.sparta.schedule.entity.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleResponseDto {
    private int schedule_id; // Schedule의 ID 필드를 매핑
    private String work;
    private int teacherId; // 변환된 teacher ID 필드
    private Integer password;
    private LocalDateTime createdDatetime; // 변환된 createdDate 필드
    private LocalDateTime updatedDatetime; // 변환된 updatedDate 필드

    // Schedule 엔티티를 기반으로 DTO를 생성
    public ScheduleResponseDto(Schedule schedule) {
        this.schedule_id = schedule.getSchedule_id();
        this.work = schedule.getWork();
        this.teacherId = schedule.getTeacher();
        this.password = schedule.getPassword();
        this.createdDatetime = schedule.getCreatedDate();
        this.updatedDatetime = schedule.getUpdatedDate();
    }

    // 이 생성자는 필요하지 않거나 적절한 위치에 맞게 수정해야 함
    public ScheduleResponseDto(String work, int teacherId, Integer password, LocalDateTime createdDatetime, LocalDateTime updatedDatetime) {
        this.work = work;
        this.teacherId = teacherId; // int로 수정
        this.password = password;
        this.createdDatetime = createdDatetime;
        this.updatedDatetime = updatedDatetime;
    }
}
