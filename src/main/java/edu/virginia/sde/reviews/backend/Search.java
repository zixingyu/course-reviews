package edu.virginia.sde.reviews.backend;

import java.util.Optional;
import java.util.Set;

public class Search {
    /*** This class serves as a wrapper for all searching course related services: search course by title,
     * by mnemonics&number, by department
     *
     */
    private final DatabaseService databaseService; // Reference to the DatabaseService

    public Search(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    // Search for courses by title
    public Optional<Set<Course>> searchCoursesByTitle(String courseTitle) {
        if (courseTitle == null || courseTitle.length() > 50) {
            throw new IllegalArgumentException("Course title must be at most 50 characters.");
        }

        // Call the database service to get courses matching the title
        try {
            // Perform the case-insensitive substring search using 'LIKE' in SQL
            return databaseService.getCoursesByTitle(courseTitle);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while searching for courses by title: " + e.getMessage(), e);
        }
    }


    // Search for courses by number
    public Optional<Set<Course>> searchCoursesByNumber(int courseNumberInput) {
        try {
            int courseNumber = Integer.parseInt(String.valueOf(courseNumberInput));
            // Validate course number as 4 digits
            if (courseNumber < 1000 || courseNumber > 9999) {
                throw new IllegalArgumentException("Course number must be a 4-digit integer between 1000 and 9999.");
            }
            // Proceed with the search in the database
            return databaseService.getCoursesByNumber(courseNumber);
        } catch (NumberFormatException e) {
            // Handle the case where the input is not a valid integer
            throw new IllegalArgumentException("Course number must be a valid integer.");
        }
    }

    // Search for courses by department
    public Optional<Set<Course>> searchCoursesByDepartment(String departmentMnemonic) {
        if (departmentMnemonic == null || departmentMnemonic.length() < 2 || departmentMnemonic.length() > 4) {
            throw new IllegalArgumentException("Department mnemonic must be within 2 - 4 characters.");
        }
        return databaseService.getCoursesByDepartment(departmentMnemonic.toUpperCase());
    }
}
