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
    private int id;
    private String work;
    private int teacher; //
    private Integer password;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private int schedule_id;

    // ScheduleRequestDto를 기반으로 Schedule 객체 생성
    public Schedule(ScheduleRequestDto requestDto) {
        this.work = requestDto.getWork();
        this.teacher = requestDto.getTeacher_Id(); // teacherId로 수정
        this.password = requestDto.getPassword();
    }

    // ScheduleRequestDto를 기반으로 Schedule 객체 업데이트
    public void update(ScheduleRequestDto requestDto) {
        this.work = requestDto.getWork();
        this.teacher = requestDto.getTeacher_Id(); // teacherId로 수정
        this.password = requestDto.getPassword();
    }
}
