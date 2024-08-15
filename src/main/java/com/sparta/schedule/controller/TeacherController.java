package com.sparta.schedule.controller;

import com.sparta.schedule.dto.ScheduleRequestDto;
import com.sparta.schedule.dto.TeacherRequestDto;
import com.sparta.schedule.entity.Teacher;
import com.sparta.schedule.manegement.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

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
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        if (!teacherRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        teacherRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
