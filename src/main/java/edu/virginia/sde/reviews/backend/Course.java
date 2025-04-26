package edu.virginia.sde.reviews.backend;

import java.util.Optional;

public record Course(String courseMnemonic, int courseNumber, String courseTitle, Optional<Double> rating) {

    public Course {
        if (courseMnemonic == null || courseMnemonic.length() > 4) {
            throw new IllegalArgumentException("Course mnemonic must be at most 4 characters.");
        }
        if (courseTitle == null || courseTitle.length() > 50) {
            throw new IllegalArgumentException("Course title must be at most 50 characters.");
        }
        if (courseNumber < 1000 || courseNumber > 9999) {
            throw new IllegalArgumentException("Course number must be a 4-digit integer.");
        }
        if (rating == null) {
            throw new IllegalArgumentException("Rating cannot be null. Use Optional.empty() if no rating is provided.");
        }
    }

    // Secondary constructor for default rating
    public Course(String courseMnemonic, int courseNumber, String courseTitle) {
        this(courseMnemonic.toUpperCase(), courseNumber, courseTitle, Optional.empty());
    }

    @Override
    public int hashCode() {
        int result = courseMnemonic.hashCode();
        result = 31 * result + courseNumber;
        result = 31 * result + courseTitle.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Course[" + courseMnemonic + " " + courseNumber + ": " + courseTitle + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Course course = (Course) obj;
        return courseNumber == course.courseNumber
                && courseMnemonic.equalsIgnoreCase(course.courseMnemonic)
                && courseTitle.equalsIgnoreCase(course.courseTitle);
    }

    @Override
    public String courseMnemonic() {
        return courseMnemonic;
    }

    @Override
    public int courseNumber() {
        return courseNumber;
    }

    @Override
    public String courseTitle() {
        return courseTitle;
    }

    @Override
    public Optional<Double> rating() {
        return rating;
    }
}
