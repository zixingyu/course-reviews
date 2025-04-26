package edu.virginia.sde.reviews.frontend;

import edu.virginia.sde.reviews.backend.Course;
import edu.virginia.sde.reviews.backend.DatabaseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddCourseController {

    @FXML
    private ComboBox<String> departmentComboBox;  // ComboBox for course department
    @FXML
    private TextField courseNumberField;          // TextField for course number
    @FXML
    private TextField courseTitleField;           // TextField for course title
    @FXML
    private Label instructionLabel;               // Label to display instructions (success/error)
    @FXML
    private Button addCourseButton;               // Add course button
    @FXML
    private Button cancelButton;                  // Cancel button

    private final DatabaseService databaseService = DatabaseService.getInstance();

    private String currentUser;

    public void initData(String user) {
        this.currentUser = user;
    }


    @FXML
    public void initialize() {
        populateDepartmentComboBox();
        addCourseButton.setOnAction(e -> addCourse());
        cancelButton.setOnAction(e -> cancelAddCourse());
    }
    public void populateDepartmentComboBox() {
        // Retrieve the list of departments from the database
        Set<String> departments = databaseService.getDepartments();
        List<String> sortedDepartments = new ArrayList<>(departments);
        sortedDepartments.sort(String::compareTo);

        // Populate the ComboBox with sorted departments
        departmentComboBox.getItems().addAll(sortedDepartments);
    }

    private void checkAlphabetic(String departmentMnemonic) {
        if (!departmentMnemonic.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("Department mnemonic must be alphabetic.");
        }
    }


    private void addCourse() {
        try {
            // Get user input
            String departmentMnemonic = departmentComboBox.getValue().toUpperCase();
            String courseNumberText = courseNumberField.getText();
            String courseTitle = courseTitleField.getText();

            // Validate inputs
            checkAlphabetic(departmentMnemonic);
            if (departmentMnemonic == null || departmentMnemonic.length() < 2 || departmentMnemonic.length() > 4) {
                throw new IllegalArgumentException("Course mnemonic must be 2-4 letters.");
            }

            int courseNumber = validateCourseNumber(courseNumberText);

            if (courseTitle == null || courseTitle.isEmpty() || courseTitle.length() > 50) {
                throw new IllegalArgumentException("Course title must be at least 1 character and at most 50 characters.");
            }

            // Create a new Course object
            Course course = new Course(departmentMnemonic.toUpperCase(), courseNumber, courseTitle);

            // Save the course to the database
            databaseService.saveCourse(course);

            // Update the instruction label to show success
            instructionLabel.setText("Course added successfully!");
            instructionLabel.setStyle("-fx-text-fill: green;");

        } catch (IllegalArgumentException e) {
            // Display validation error message
            instructionLabel.setText(e.getMessage());
            instructionLabel.setStyle("-fx-text-fill: red;");
        } catch (RuntimeException e) {
            // Handle other runtime errors (eg. duplicate course error)
            instructionLabel.setText("Course already exists. Please enter a new course.");
            instructionLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            // Handle any other errors (e.g., database issues)
            instructionLabel.setText("Failed to add course: " + "course already added.");
            instructionLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private int validateCourseNumber(String courseNumberText) {
        // Validate the course number
        try {
            int courseNumber = Integer.parseInt(courseNumberText);
            if (courseNumber < 1000 || courseNumber > 9999) {
                throw new IllegalArgumentException("Course number must be a 4-digit integer.");
            }
            return courseNumber;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Course number must be an integer.");
        }
    }

    private void cancelAddCourse() {
        try {
            // Load the course search scene (you would need a FXML file for that screen)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml")); // Update path to your course search FXML file
            Parent courseSearchRoot = loader.load();  // Load the Course Search screen

            // Pass currentUser back to CourseSearchController
            CourseSearchController controller = loader.getController();
            controller.setCurrentUser(currentUser); // Pass the user back

            // Get the current stage (window)
            Stage stage = (Stage) cancelButton.getScene().getWindow();

            // Set the new scene (course search screen)
            Scene scene = new Scene(courseSearchRoot);
            stage.setScene(scene);
            stage.show();  // Show the new scene

            // Optionally, clear the form:
            departmentComboBox.getSelectionModel().clearSelection();
            courseNumberField.clear();
            courseTitleField.clear();
            instructionLabel.setText("");  // Clear any instruction or error messages
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
