package com.sparta.schedule.controller;

import com.sparta.schedule.dto.ScheduleRequestDto;
import com.sparta.schedule.dto.ScheduleResponseDto;
import com.sparta.schedule.entity.Schedule;
import com.sparta.schedule.entity.Teacher;
import com.sparta.schedule.manegement.TeacherRepository;
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
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final JdbcTemplate jdbcTemplate;
    private final TeacherRepository teacherRepository;

    public ScheduleController(JdbcTemplate jdbcTemplate, TeacherRepository teacherRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.teacherRepository = teacherRepository;
    }

    @PostMapping("/schedules")
    public ScheduleResponseDto createSchedule(@RequestBody ScheduleRequestDto requestDto) {
        Integer teacherId = requestDto.getTeacherId();
        if (teacherId == null) {
            throw new IllegalArgumentException("담당자 ID가 제공되지 않았습니다.");
        }

        // RequestDto에서 teacherId를 사용하여 Entity 조회
        Optional<Teacher> teacherOptional = teacherRepository.findById(teacherId);
        if (teacherOptional.isEmpty()) {
            // 로그를 추가하여 문제를 추적할 수 있음
            System.err.println("유효하지 않은 담당자 ID: " + teacherId);
            throw new IllegalArgumentException("유효하지 않은 담당자입니다.");
        }

        Teacher teacher = teacherOptional.get();

        // 추가 유효성 검사 (예: work와 password의 값 검사)
        if (requestDto.getWork() == null || requestDto.getWork().trim().isEmpty()) {
            throw new IllegalArgumentException("작업 내용이 비어있습니다.");
        }

        Schedule schedule = new Schedule();
        schedule.setWork(requestDto.getWork());
        schedule.setTeacher(teacher.getId());
        schedule.setPassword(requestDto.getPassword());

        // DB 저장
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = "INSERT INTO schedule (work, password, teacher_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, schedule.getWork());
            preparedStatement.setInt(2, schedule.getPassword());
            preparedStatement.setInt(3, teacher.getId());
            return preparedStatement;
        }, keyHolder);

        schedule.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        return new ScheduleResponseDto(schedule, teacher);
    }


    @GetMapping("/schedules")
    public List<ScheduleResponseDto> getSchedules() {
        String sql = "SELECT s.schedule_id, s.work, s.password, s.createdDate, s.updatedDate, t.id AS teacher_id, t.teacher_name, t.teacher_email "
                + "FROM schedule s JOIN teacher t ON s.teacher_id = t.id "
                + "ORDER BY s.updatedDate DESC";

        List<ScheduleResponseDto> query = jdbcTemplate.query(sql, new RowMapper<ScheduleResponseDto>() {
            @Override
            public ScheduleResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                Schedule schedule = new Schedule();
                schedule.setId(rs.getInt("schedule_id"));
                schedule.setWork(rs.getString("work"));
                schedule.setPassword(rs.getInt("password"));
                schedule.setCreatedDate(rs.getObject("createdDate", LocalDateTime.class));
                schedule.setUpdatedDate(rs.getObject("updatedDate", LocalDateTime.class));

                Teacher teacher = new Teacher();
                teacher.setId(rs.getInt("teacher_id"));
                teacher.setName(rs.getString("teacher_name"));
                teacher.setEmail(rs.getString("teacher_email"));

                schedule.setTeacher(teacher.getId());

                return new ScheduleResponseDto(schedule, teacher);
            }
        });
        return query;
    }



    @PutMapping("/schedules/{scheduleId}")
    public String updateSchedule(@PathVariable int scheduleId, @RequestBody ScheduleRequestDto requestDto) {
        // 해당 스케줄이 DB에 존재하는지 확인
        Schedule schedule = findByScheduleId(scheduleId);

        // 비밀번호가 올바른지 확인
        if (schedule == null || !schedule.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        Optional<Teacher> teacherOptional = teacherRepository.findById(requestDto.getTeacherId());
        if (teacherOptional.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 담당자입니다.");
        }

        // schedule 내용 수정
        String sql = "UPDATE schedule SET teacher_id = ?, password = ?, updatedDate = ?, work = ? WHERE schedule_id = ?";
        jdbcTemplate.update(sql, requestDto.getTeacherId(), requestDto.getPassword(), LocalDateTime.now(), requestDto.getWork(), scheduleId); // 변경: teacherId를 사용

        return String.valueOf(scheduleId);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public String deleteSchedule(@PathVariable int scheduleId) {
        Schedule schedule = findByScheduleId(scheduleId);
        if (schedule != null) {
            // schedule 삭제
            String sql = "DELETE FROM schedule WHERE schedule_id = ?";
            jdbcTemplate.update(sql, scheduleId);

            return String.valueOf(scheduleId);
        } else {
            throw new IllegalArgumentException("선택한 일정은 존재하지 않습니다.");
        }
    }

    private Schedule findByScheduleId(int scheduleId) {
        String sql = "SELECT * FROM schedule WHERE schedule_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Schedule schedule = new Schedule();
                schedule.setId(rs.getInt("schedule_id"));
                schedule.setWork(rs.getString("work"));
                schedule.setPassword(rs.getInt("password"));

                int teacherId = rs.getInt("teacher"); // 변경: 'teacher' 필드를 가져옴
                Optional<Teacher> teacherOptional = teacherRepository.findById(teacherId);
                if (teacherOptional.isPresent()) {
                    schedule.setTeacher(teacherId); // 변경: teacherId를 저장
                }

                return schedule;
            }, scheduleId);
        } catch (Exception e) {
            return null;
        }
    }
}
