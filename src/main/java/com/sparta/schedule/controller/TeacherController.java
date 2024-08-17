package com.sparta.schedule.controller;

import com.sparta.schedule.dto.TeacherRequestDto;
import com.sparta.schedule.entity.Teacher;
import com.sparta.schedule.management.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // POST 메서드: 새로운 담당자 추가
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(@Validated @RequestBody TeacherRequestDto requestDto) {
        Teacher teacher = new Teacher();
        teacher.setName(requestDto.getName());
        teacher.setEmail(requestDto.getEmail());
        teacher.setCreatedDate(LocalDateTime.now());
        teacher.setUpdatedDate(LocalDateTime.now());

        Teacher savedTeacher = teacherRepository.save(teacher);
        return ResponseEntity.ok(savedTeacher);
    }

    // DELETE 메서드: 담당자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Integer id) {

        try {
            // 교사 객체를 데이터베이스에서 찾음
            Optional<Teacher> optionalTeacher = teacherRepository.findById(id);
            if (!optionalTeacher.isPresent()) {
                System.out.println("teacher id 값이 없습니다.");
                return ResponseEntity.notFound().build(); // 404 Not Found 반환
            }

            // 교사 객체가 존재하면 삭제
            String sql = "DELETE FROM teacher WHERE id = ?";
            jdbcTemplate.update(sql, id); // 삭제 수행
            System.out.println("삭제완료");
            return ResponseEntity.noContent().build(); // 204 No Content 반환

        } catch (Exception e) {
            System.out.println("오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).build(); // 500 Internal Server Error 반환
        }
    }
}
