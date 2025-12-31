package com.uninest.model.dao;

import com.uninest.model.Subject;
import com.uninest.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO {

    private Subject map(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(rs.getInt("subject_id"));
        subject.setCommunityId(rs.getInt("community_id"));
        subject.setName(rs.getString("name"));
        subject.setDescription(rs.getString("description"));
        subject.setCode(rs.getString("code"));
        subject.setAcademicYear(rs.getInt("academic_year"));
        subject.setSemester(rs.getInt("semester"));
        subject.setCredits(rs.getInt("credits"));
        subject.setStatus(rs.getString("status"));
        subject.setCreatedAt(rs.getTimestamp("created_at"));
        return subject;
    }

    public Optional<Subject> findById(int subjectId) {
        String sql = "SELECT * FROM subjects WHERE subject_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching subject", e);
        }
        return Optional.empty();
    }

    public List<Subject> findByCommunityId(int communityId) {
        String sql = "SELECT * FROM subjects WHERE community_id = ? ORDER BY semester, academic_year, name";
        List<Subject> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, communityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing subjects", e);
        }
        return list;
    }

    public int create(Subject subject) {
        String sql = "INSERT INTO subjects(community_id, name, description, code, academic_year, semester, status) VALUES(?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, subject.getCommunityId());
            ps.setString(2, subject.getName());
            ps.setString(3, subject.getDescription());
            ps.setString(4, subject.getCode());
            ps.setInt(5, subject.getAcademicYear());
            ps.setInt(6, subject.getSemester());
            ps.setString(7, subject.getStatus() != null ? subject.getStatus() : "upcoming");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    subject.setSubjectId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating subject", e);
        }
        return 0;
    }

    public boolean update(Subject subject) {
        String sql = "UPDATE subjects SET name = ?, description = ?, code = ?, academic_year = ?, semester = ?, status = ? WHERE subject_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, subject.getName());
            ps.setString(2, subject.getDescription());
            ps.setString(3, subject.getCode());
            ps.setInt(4, subject.getAcademicYear());
            ps.setInt(5, subject.getSemester());
            ps.setString(6, subject.getStatus());
            ps.setInt(7, subject.getSubjectId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating subject", e);
        }
    }

    public boolean delete(int subjectId) {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting subject", e);
        }
    }
}
