package com.uninest.controller.student;

import com.uninest.model.Grade;
import com.uninest.model.Subject;
import com.uninest.model.User;
import com.uninest.model.dao.GradeDAO;
import com.uninest.model.dao.SubjectDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ProgressAnalysisServlet", urlPatterns = { "/student/progress-analysis", "/student/gpa-calculator" })
public class ProgressAnalysisServlet extends HttpServlet {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final GradeDAO gradeDAO = new GradeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (user.getCommunityId() == null) {
            resp.sendRedirect(req.getContextPath() + "/student/join-community");
            return;
        }

        // Fetch subjects for the student's community
        List<Subject> subjects = subjectDAO.findByCommunityId(user.getCommunityId());

        // Fetch existing grades for the student
        List<Grade> grades = gradeDAO.findByUserId(user.getId());

        // Create a map for easy lookup in JSP
        Map<Integer, Grade> gradeMap = new HashMap<>();
        for (Grade grade : grades) {
            gradeMap.put(grade.getSubjectId(), grade);
        }

        // Calculate GPA
        BigDecimal gpa = calculateGPA(grades);

        req.setAttribute("subjects", subjects);
        req.setAttribute("gradeMap", gradeMap);
        req.setAttribute("gpa", gpa);

        req.getRequestDispatcher("/WEB-INF/views/student/gpa-calculator.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Iterate over parameters to find grade inputs
        // Expected format: grade_{subjectId} = {gradeLetter}
        Map<String, String[]> parameterMap = req.getParameterMap();

        for (String paramName : parameterMap.keySet()) {
            if (paramName.startsWith("grade_")) {
                try {
                    int subjectId = Integer.parseInt(paramName.substring(6)); // remove "grade_" prefix
                    String gradeLetter = req.getParameter(paramName);

                    if (gradeLetter != null && !gradeLetter.trim().isEmpty()) {
                        BigDecimal gradePoint = getGradePoint(gradeLetter);

                        Grade grade = new Grade();
                        grade.setUserId(user.getId());
                        grade.setSubjectId(subjectId);
                        grade.setGradeLetter(gradeLetter);
                        grade.setGradePoint(gradePoint);

                        gradeDAO.saveOrUpdate(grade);
                    }
                } catch (NumberFormatException | java.sql.SQLException e) {
                    e.printStackTrace();
                    // Continue processing other grades even if one fails
                }
            }
        }

        // Redirect to avoid form resubmission
        resp.sendRedirect(req.getRequestURI() + "?success=true");
    }

    private BigDecimal calculateGPA(List<Grade> grades) {
        BigDecimal totalPoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (Grade grade : grades) {
            if (grade.getSubject() != null && grade.getGradePoint() != null) {
                int credits = grade.getSubject().getCredits();
                // Skip subjects with 0 credits (e.g. non-credit courses)
                if (credits > 0) {
                    BigDecimal points = grade.getGradePoint().multiply(new BigDecimal(credits));
                    totalPoints = totalPoints.add(points);
                    totalCredits += credits;
                }
            }
        }

        if (totalCredits == 0) {
            return BigDecimal.ZERO;
        }

        return totalPoints.divide(new BigDecimal(totalCredits), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getGradePoint(String letter) {
        if (letter == null)
            return BigDecimal.ZERO;

        switch (letter.toUpperCase().trim()) {
            case "A+":
                return new BigDecimal("4.00");
            case "A":
                return new BigDecimal("4.00");
            case "A-":
                return new BigDecimal("3.70");
            case "B+":
                return new BigDecimal("3.30");
            case "B":
                return new BigDecimal("3.00");
            case "B-":
                return new BigDecimal("2.70");
            case "C+":
                return new BigDecimal("2.30");
            case "C":
                return new BigDecimal("2.00");
            case "C-":
                return new BigDecimal("1.70");
            case "D+":
                return new BigDecimal("1.30");
            case "D":
                return new BigDecimal("1.00");
            case "E":
                return BigDecimal.ZERO;
            case "F":
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }
}
