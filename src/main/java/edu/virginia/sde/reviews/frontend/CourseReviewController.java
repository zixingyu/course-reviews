package edu.virginia.sde.reviews.frontend;

import edu.virginia.sde.reviews.backend.Course;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CourseReviewController { //Placeholder for now

    @FXML
    private Label courseTitleLabel;  // Label for course title
    @FXML
    private Label courseMnemonicLabel;  // Label for course mnemonic (e.g., MEMO)
    @FXML
    private Label courseNumberLabel;  // Label for course number
    @FXML
    private Label courseRatingLabel;  // Label for course rating

    private Course course;

    // This method will be called by the CourseListController to pass the selected course
    public void setCourse(Course course) {
        this.course = course;
        displayCourseDetails();
    }

    // This method updates the UI with the course details
    private void displayCourseDetails() {
        courseTitleLabel.setText(course.courseTitle());
        courseMnemonicLabel.setText(course.courseMnemonic());
        courseNumberLabel.setText(String.valueOf(course.courseNumber()));
        courseRatingLabel.setText(String.format("%.2f", course.rating()));  // Assuming rating is a Double
    }
}