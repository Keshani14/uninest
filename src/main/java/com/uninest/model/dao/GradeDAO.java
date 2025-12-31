package com.uninest.model.dao;

import com.uninest.model.Grade;
import com.uninest.model.Subject;
import com.uninest.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    public void saveOrUpdate(Grade grade) throws SQLException {
        String query = "INSERT INTO student_grades (user_id, subject_id, grade_letter, grade_point) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE grade_letter = ?, grade_point = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, grade.getUserId());
            pstmt.setInt(2, grade.getSubjectId());
            pstmt.setString(3, grade.getGradeLetter());
            pstmt.setBigDecimal(4, grade.getGradePoint());

            // Update part
            pstmt.setString(5, grade.getGradeLetter());
            pstmt.setBigDecimal(6, grade.getGradePoint());

            pstmt.executeUpdate();
        }
    }

    public List<Grade> findByUserId(int userId) {
        List<Grade> grades = new ArrayList<>();
        String query = "SELECT g.*, s.name as subject_name, s.code as subject_code, " +
                "s.credits, s.academic_year, s.semester " +
                "FROM student_grades g " +
                "JOIN subjects s ON g.subject_id = s.subject_id " +
                "WHERE g.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(mapResultSetToGrade(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setUserId(rs.getInt("user_id"));
        grade.setSubjectId(rs.getInt("subject_id"));
        grade.setGradeLetter(rs.getString("grade_letter"));
        grade.setGradePoint(rs.getBigDecimal("grade_point"));
        grade.setCreatedAt(rs.getTimestamp("created_at"));

        // Map Subject details if available
        Subject subject = new Subject();
        subject.setSubjectId(rs.getInt("subject_id"));
        subject.setName(rs.getString("subject_name"));
        subject.setCode(rs.getString("subject_code"));
        subject.setCredits(rs.getInt("credits"));
        subject.setAcademicYear(rs.getInt("academic_year"));
        subject.setSemester(rs.getInt("semester"));

        grade.setSubject(subject);

        return grade;
    }
}
