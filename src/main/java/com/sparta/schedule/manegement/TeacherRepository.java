package com.sparta.schedule.manegement;

import com.sparta.schedule.entity.Teacher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class TeacherRepository {

    private final JdbcTemplate jdbcTemplate;

    public TeacherRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Teacher> findById(int id){
        String sql = "SELECT*FROM teacher WHERE id =?";
        try{
            Teacher teacher = jdbcTemplate.queryForObject(sql, new TeacherRowMapper(), id);
            return Optional.of(teacher);
        }catch (Exception e){
            return Optional.empty();
        }
    }

    public Teacher save(Teacher teacher){
        String sql = "INSERT INTO teacher (name, email, createdDate, updatedDate) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, teacher.getName(), teacher.getEmail(), teacher.getCreatedDate(), teacher.getUpdatedDate());
        return teacher;
    }

    public boolean existsById(Long id) {
        return false;
    }

    public void deleteById(Long id) {
    }

    private static class TeacherRowMapper implements RowMapper<Teacher>{
        @Override
        public Teacher mapRow(ResultSet rs, int rowNum) throws SQLException {
            Teacher teacher = new Teacher();
            teacher.setId(rs.getInt("id"));
            teacher.setName(rs.getString("name"));
            teacher.setEmail(rs.getString("email"));
            teacher.setCreatedDate(rs.getObject("createdDate", LocalDateTime.class));
            teacher.setUpdatedDate(rs.getObject("updatedDate", LocalDateTime.class));

            return teacher;
        }
    }
}
