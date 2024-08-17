package com.sparta.schedule.controller;

import com.sparta.schedule.dto.ScheduleRequestDto;
import com.sparta.schedule.dto.ScheduleResponseDto;
import com.sparta.schedule.entity.Schedule;
import com.sparta.schedule.entity.Teacher;
import com.sparta.schedule.management.TeacherRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    public JdbcTemplate jdbcTemplate;
    private final TeacherRepository teacherRepository;

    public ScheduleController(JdbcTemplate jdbcTemplate, TeacherRepository teacherRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.teacherRepository = teacherRepository;
    }

    @PostMapping("/schedules")
    public ScheduleResponseDto createSchedule(@RequestBody ScheduleRequestDto requestDto) {
        Integer teacherId = requestDto.getTeacher_Id();
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

        return new ScheduleResponseDto(schedule);
    }

    @GetMapping("/schedules")
    public List<ScheduleResponseDto> getSchedules() {
        String sql = "SELECT s.schedule_id, s.work, s.password, s.createdDate, s.updatedDate, t.id AS teacher_id, t.name AS teacher_name, t.email AS teacher_email "
                + "FROM schedule s JOIN teacher t ON s.teacher_id = t.id "
                + "ORDER BY s.updatedDate DESC";
        try {
            List<ScheduleResponseDto> query = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Schedule schedule = new Schedule();
                schedule.setId(rs.getInt("schedule_id"));
                schedule.setWork(rs.getString("work"));
                schedule.setPassword(rs.getInt("password"));
                schedule.setCreatedDate(rs.getObject("createdDate", LocalDateTime.class));
                schedule.setUpdatedDate(rs.getObject("updatedDate", LocalDateTime.class));

                Teacher teacher = new Teacher();
                teacher.setId(rs.getInt("teacher_id")); // 수정된 필드 이름 사용
                teacher.setName(rs.getString("teacher_name")); // 수정된 필드 이름 사용
                teacher.setEmail(rs.getString("teacher_email")); // 수정된 필드 이름 사용

                schedule.setTeacher(teacher.getId());
                return new ScheduleResponseDto(schedule);
            });
            return query;

        }catch (Exception e){
                throw new RuntimeException("일정을 찾을 수 없습니다", e);
            }

    }

    @PutMapping("/schedules/{scheduleId}")
    public Schedule updateSchedule(@PathVariable int scheduleId, @RequestBody ScheduleRequestDto requestDto) {
        // 해당 스케줄이 DB에 존재하는지 확인
        Schedule schedule = findByScheduleId(scheduleId);

        // 비밀번호가 올바른지 확인
        if (schedule == null || !schedule.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // schedule 내용 수정
        String sql = "UPDATE schedule SET updatedDate = ?, work = ? WHERE schedule_id = ? AND Password = ?";
        jdbcTemplate.update(sql, LocalDateTime.now(), requestDto.getWork(), scheduleId, requestDto.getPassword()); // 변경: teacherId를 사용

        return findByScheduleId(scheduleId);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public Schedule deleteSchedule(@PathVariable int scheduleId, @RequestParam int password) {
        Schedule schedule = findByScheduleId(scheduleId);
            if(schedule == null){
                System.out.println("schedule_Id 불일치");
            }
            if(schedule.getPassword() != password){
                System.out.println("비밀번호가 틀립니다.");
                return null;
            }
            try {
                // schedule 삭제
                String sql = "DELETE FROM schedule WHERE schedule_id AND password = ?";
                jdbcTemplate.update(sql, scheduleId, password);
            }catch (Exception e) {
                System.out.println("삭제 완료"+e.getMessage());
                return null;
            }
        return schedule;
    }

    public Schedule findByScheduleId(@RequestParam int scheduleId) {
        String sql = "SELECT * FROM schedule s join teacher t on t.id = s.schedule_id WHERE schedule_id = ?";
        System.out.println("scheduleId = " + scheduleId);

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Schedule schedule = new Schedule();
                schedule.setId(rs.getInt("schedule_id"));
                schedule.setWork(rs.getString("work"));
                schedule.setPassword(rs.getInt("password"));

                int teacherId = rs.getInt("id"); // 변경: 'teacher' 필드를 가져옴
                return schedule;
            }, scheduleId);
    }
}
