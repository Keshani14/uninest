-- Add credits column to subjects table
ALTER TABLE `subjects` ADD COLUMN `credits` INT NOT NULL DEFAULT 3 AFTER `code`;

-- Create student_grades table
CREATE TABLE `student_grades` (
  `grade_id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `subject_id` INT NOT NULL,
  `grade_letter` VARCHAR(5) NOT NULL,
  `grade_point` DECIMAL(3,2) NOT NULL,
  `semester` VARCHAR(20) NOT NULL, -- To cache the semester context if needed, though subject has it.
  -- Actually, we can just rely on subject_id for metadata.
  -- But wait, the user wants "1st Year", "2nd Year" in UI.
  -- The subject table has `academic_year` (int) and `semester` (int).
  -- We just strictly link to subject_id.
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`grade_id`),
  UNIQUE KEY `unique_user_subject` (`user_id`, `subject_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`subject_id`) REFERENCES `subjects`(`subject_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
