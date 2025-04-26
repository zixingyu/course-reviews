package edu.virginia.sde.reviews.frontend;

import edu.virginia.sde.reviews.backend.ComboBoxAutocomplete;
import edu.virginia.sde.reviews.backend.Course;
import edu.virginia.sde.reviews.backend.Search;
import edu.virginia.sde.reviews.backend.DatabaseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class CourseSearchController {

    @FXML
    private ComboBox<String> departmentComboBox;

    @FXML
    private TextField courseNumberField;

    @FXML
    private TextField courseTitleField;

    @FXML
    private Button addCourseButton;

    @FXML
    private Button myReviewsButton;

    @FXML
    private Button logOutButton;

    @FXML
    private Button searchButton;

    @FXML
    private Label instructionLabel;

    private Search searchService;

    private String currentUser;

    public void setCurrentUser(String user) {
        this.currentUser = user;
        //System.out.println("CourseSearchController: currentUser = " + currentUser);
    }

    public void initialize() {
        // Initialize Search with DatabaseService
        DatabaseService databaseService = DatabaseService.getInstance();
        searchService = new Search(databaseService);

        // Populate department ComboBox (replace with actual department retrieval logic)
        Set<String> departments = databaseService.getDepartments();
        List<String> sortedDepartments = new ArrayList<>(departments);
        sortedDepartments.sort(String::compareTo);

        // Setup ComboBox autocomplete
        ComboBoxAutocomplete.autoCompleteComboBoxPlus(departmentComboBox, (typedText, itemToCompare) -> itemToCompare.toLowerCase().contains(typedText.toLowerCase()));

        // Populate the ComboBox with sorted departments
        departmentComboBox.getItems().addAll(sortedDepartments);

        // Register Enter key handlers for search fields
        registerEnterKeyHandlers();
    }

    @FXML
    private void handleSearch() throws IOException {
        String department = ComboBoxAutocomplete.getComboBoxValue(departmentComboBox);
        String courseNumber = courseNumberField.getText().trim();
        String courseTitle = courseTitleField.getText().trim();

        // Base Set to hold the combined search results
        Set<Course> combinedResults = new HashSet<>();

        // Check if at least one search criterion is provided
        if ((department == null || department.isEmpty()) && courseNumber.isEmpty() && courseTitle.isEmpty()) {
            instructionLabel.setText("Please enter at least one search criterion (department, course number, or course title).");
            return;
        }

        try {
            // Search by department if provided
            Optional<Set<Course>> departmentResults = Optional.empty();
            if (department != null && !department.isEmpty()) {
                departmentResults = searchService.searchCoursesByDepartment(department);
            }

            // Search by course number if provided
            Optional<Set<Course>> numberResults = Optional.empty();
            if (!courseNumber.isEmpty()) {
                try {
                    int number = Integer.parseInt(courseNumber);
                    numberResults = searchService.searchCoursesByNumber(number);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Course number must be a valid integer.");
                }
            }

            // Search by course title if provided
            Optional<Set<Course>> titleResults = Optional.empty();
            if (!courseTitle.isEmpty()) {
                titleResults = searchService.searchCoursesByTitle(courseTitle);
            }

            // Combine results based on provided filters
            if (departmentResults.isPresent()) {
                combinedResults.addAll(departmentResults.get());
            }
            if (numberResults.isPresent()) {
                if (combinedResults.isEmpty()) {
                    combinedResults.addAll(numberResults.get());
                } else {
                    combinedResults.retainAll(numberResults.get());
                }
            }
            if (titleResults.isPresent()) {
                if (combinedResults.isEmpty()) {
                    combinedResults.addAll(titleResults.get());
                } else {
                    combinedResults.retainAll(titleResults.get());
                }
            }

        } catch (IllegalArgumentException e) {
            // Handle invalid input
            instructionLabel.setText(e.getMessage());
            return;
        }

        // Handle no results case
        if (combinedResults.isEmpty()) {
            instructionLabel.setText("No courses found for your search criteria.");
            return;
        }

        // Convert Set to List to allow sorting
        List<Course> sortedCourses = new ArrayList<>(combinedResults);

        sortedCourses.sort(Comparator.comparing((Course c) -> c.courseMnemonic().toUpperCase())
                .thenComparingInt(Course::courseNumber));


        // Navigate to the Course List scene with the sorted results
        System.out.println("Number of courses found: " + sortedCourses.size());
        navigateToCourseList(sortedCourses); // Pass sorted list to the next scene
    }


    @FXML
    private void handleAddCourse() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/AddCourse.fxml"));
        Parent root = loader.load();

        // Retrieve the AddCourseController and set the current user
        AddCourseController controller = loader.getController();
        controller.initData(currentUser); // Pass the user to AddCourseController

        Stage stage = (Stage) addCourseButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleMyReviews() throws IOException {
        System.out.println("Navigating to MyReviews with user: " + currentUser);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/MyReviews.fxml"));
        Parent root = loader.load();

        MyReviewsController controller = loader.getController();

        // Pass currentUser to MyReviewsController
        controller.initData(currentUser);

        Stage stage = (Stage) myReviewsButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    @FXML
    private void handleLogOut() throws IOException {
        navigateToScene("/edu/virginia/sde/reviews/Log-inScreen.fxml");
    }

    private void navigateToCourseList(List<Course> courses) throws IOException {
        // Logic to pass `courses` to the Course List scene
        Stage stage = (Stage) departmentComboBox.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseList.fxml"));
        Scene scene = new Scene(loader.load());

        // Pass courses to CourseListController
        CourseListController controller = loader.getController();
        controller.setCourses(courses); // Ensure courses are passed
        controller.setCurrentUser(currentUser); // Pass currentUser
        stage.setScene(scene);
    }

    private void navigateToScene(String fxmlFile) throws IOException {
        Stage stage = (Stage) departmentComboBox.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
    }

    private void registerEnterKeyHandlers() {
        registerEnterKeyHandler(departmentComboBox);
        registerEnterKeyHandler(courseNumberField);
        registerEnterKeyHandler(courseTitleField);
    }

    private void registerEnterKeyHandler(Control control) {
        if (control instanceof ComboBox || control instanceof TextField) {
            control.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    try {
                        handleSearch();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            throw new RuntimeException("Unsupported control type \""
                    + control.getClass() + "\" for Enter key handler registration.");
        }
    }
}
