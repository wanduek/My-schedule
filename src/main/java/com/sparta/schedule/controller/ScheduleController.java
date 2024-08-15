package com.sparta.schedule.controller;

import com.sparta.schedule.dto.ScheduleRequestDto;
import com.sparta.schedule.dto.ScheduleResponseDto;
import com.sparta.schedule.entity.Schedule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final JdbcTemplate jdbcTemplate;

    public ScheduleController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/schedules")
    public ScheduleResponseDto createSchedule(@RequestBody ScheduleRequestDto requestDto) {
        // RequestDto -> Entity
        Schedule schedule = new Schedule(requestDto);

        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();

        // DB 저장
        KeyHolder keyHolder = new GeneratedKeyHolder(); // 기본 키를 반환받기 위한 객체

        String sql = "INSERT INTO schedule (work, password, teacher, createdDate, updatedDate) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, schedule.getWork());
            preparedStatement.setInt(2, schedule.getPassword());
            preparedStatement.setString(3, schedule.getTeacher()); // `work` 필드 설정
            preparedStatement.setObject(4, now); // createdDate와 updatedDate는 현재시간에 설정
            preparedStatement.setObject(5, now);
            return preparedStatement;
        }, keyHolder);

        // DB Insert 후 받아온 기본키 확인
//        int schedule_id = (int) keyHolder.getKey();
//        schedule.setSchedule_id(schedule_id);
        schedule.setSchedule_id(Objects.requireNonNull(keyHolder.getKey()).intValue());
        // Entity -> ResponseDto
        ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto(schedule);

        return scheduleResponseDto;
    }

    @GetMapping("/schedules")
    public List<ScheduleResponseDto> getSchedules() {
        String sql = "SELECT * FROM schedule";

        return jdbcTemplate.query(sql, new RowMapper<ScheduleResponseDto>() {
            @Override
            public ScheduleResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                String work = rs.getString("work");
                String teacher = rs.getString("teacher");
                Integer password = rs.getInt("password");
                return new ScheduleResponseDto(work, teacher, password);
            }
        });
    }

    @PutMapping("/schedules/{scheduleId}")
    public String updateSchedule(@PathVariable String scheduleId, @RequestBody ScheduleRequestDto requestDto) {
        // 해당 스케줄이 DB에 존재하는지 확인
        Schedule schedule = findByWork(scheduleId);

        //비밀번호가 올바른지 확인
        if(!schedule.getPassword().equals(requestDto.getPassword())){
            throw new IllegalArgumentException("비밀번호가 올바릅니다.");
        }

        if (schedule != null) {
            // schedule 내용 수정)

            //스케줄 내용 수정
            String sql = "UPDATE schedule SET teacher = ?, password = ? ,updatedDate = ?, work = ? where schedule_id = ?";
            jdbcTemplate.update(sql, requestDto.getTeacher(), requestDto.getPassword(), LocalDateTime.now(), scheduleId);

            return scheduleId;
        } else {
            throw new IllegalArgumentException("선택한 일정은 존재하지 않습니다.");
        }
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public String deleteSchedule(@PathVariable String scheduleId) {
        Schedule schedule = findByWork(scheduleId);
        if (schedule != null) {
            // schedule 삭제
            String sql = "DELETE FROM schedule WHERE schedule_id = ?";
            jdbcTemplate.update(sql, scheduleId);

            return scheduleId;
        } else {
            throw new IllegalArgumentException("선택한 일정은 존재하지 않습니다.");
        }
    }

    private Schedule findByWork(int schedule_id) {
        String sql = "SELECT * FROM schedule WHERE work = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Schedule schedule = new Schedule();
                schedule.setWork(rs.getString("work"));
                schedule.setTeacher(rs.getString("teacher"));
                schedule.setPassword(rs.getInt("password"));
                return schedule;
            }, schedule_id);
        }catch (Exception e){
            return null; //레코드가 없는 경우 처리
        }
    }

}
