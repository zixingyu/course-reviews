package edu.virginia.sde.reviews.frontend;

import edu.virginia.sde.reviews.backend.Course;
import edu.virginia.sde.reviews.backend.DatabaseService;
import edu.virginia.sde.reviews.backend.Review;
import edu.virginia.sde.reviews.backend.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Set;

public class ReviewsController {

    @FXML
    private Button AddCourseButton;

    @FXML
    private Label AverageRatingLabel;

    @FXML
    private Button BackButton;

    @FXML
    private Label CourseTitleLabel;

    @FXML
    private Button DeleteButton;

    @FXML
    private Button LogoutButton;

    @FXML
    private Button MyReviewsButton;

    @FXML
    private Label ValidRatingLabel;

    @FXML
    private TextArea RatingTextArea;

    @FXML
    private TextArea ReviewContentField;

    @FXML
    private ListView<String> reviewsListView;

    @FXML
    private Button SubmitButton;

    private DatabaseService databaseService;
    private String currentUser;
    private String currentCourseMnemonic;
    private int currentCourseNumber;
    private String currentCourseTitle;
    private Review userReview;
    private static final String FXML_BASE_PATH = "/edu/virginia/sde/reviews/";

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        System.out.println("DatabaseService initialized: " + (databaseService != null));
    }


    /**
     * Initialize the controller with user and course information passed from the previous scene.
     */
    public void initData(String user, String courseMnemonic, int courseNumber, String courseTitle) {
//        if (user == null || user.isEmpty()) {
//            throw new IllegalArgumentException("User cannot be null or empty.");
//        }
        this.currentUser = user;
        this.currentCourseMnemonic = courseMnemonic;
        this.currentCourseNumber = courseNumber;
        this.currentCourseTitle = courseTitle;

        databaseService = DatabaseService.getInstance();
        displayCourseInfo();
        loadReviews();
        checkUserReview();
    }

    private Course course;

    // This method will be called by the CourseListController to pass the selected course
    public void setCourse(Course course) {
        this.course = course;
        if (course != null) {
            this.currentCourseMnemonic = course.courseMnemonic();
            this.currentCourseNumber = course.courseNumber();
            this.currentCourseTitle = course.courseTitle();

            CourseTitleLabel.setText(String.format("%s %d: %s",
                    course.courseMnemonic(), course.courseNumber(), course.courseTitle()));
            loadAverageRating(); // Calculate and display the average rating
//            loadReviews();
//            checkUserReview();
        } else {
            CourseTitleLabel.setText("No course selected");
            AverageRatingLabel.setText(" ");
        }
    }
    private void loadAverageRating() {
        Set<Review> reviews = DatabaseService.getInstance()
                .getReviewsByCourse(course)
                .orElse(Set.of());

        if (reviews.isEmpty()) {
            AverageRatingLabel.setText(" ");
            return;
        }

        double totalRating = 0.0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }

        double averageRating = totalRating / reviews.size();
        AverageRatingLabel.setText(String.format("%.2f", averageRating));
    }


    private void displayCourseInfo() {
        CourseTitleLabel.setText(String.format("%s %d: %s", currentCourseMnemonic, currentCourseNumber, currentCourseTitle));
    }

    private void loadReviews() {
        Set<Review> reviews = databaseService.getReviewsByCourse(new Course(currentCourseMnemonic, currentCourseNumber, currentCourseTitle)).orElse(Set.of());
        ObservableList<String> reviewItems = FXCollections.observableArrayList();

        double totalRating = 0.0;
        int count = 0;

        for (Review review : reviews) {
            totalRating += review.getRating();
            count++;
            reviewItems.add(formatReview(review));
        }

        reviewsListView.setItems(reviewItems);
        AverageRatingLabel.setText(count > 0 ? String.format("%.2f", totalRating / count) : " ");
    }

private void checkUserReview() {
    userReview = databaseService.getReviewByUserAndCourse(
            new User(currentUser, ""),
            new Course(currentCourseMnemonic, currentCourseNumber, currentCourseTitle)
    ).orElse(null);

    if (userReview != null) {
        RatingTextArea.setText(String.valueOf(userReview.getRating()));
        ReviewContentField.setText(userReview.getReviewContent());
    }
}

    private String formatReview(Review review) {
        return String.format("Rating: %d\nTimestamp: %s\nComment: %s",
                review.getRating(),
                review.getTimestamp().toString(),
                review.getReviewContent().isEmpty() ? "No comment" : review.getReviewContent());
    }

    @FXML
    void submitReview(ActionEvent event) {
        try {
            System.out.println("currentCourseMnemonic: " + currentCourseMnemonic);
            System.out.println("currentCourseNumber: " + currentCourseNumber);
            System.out.println("currentCourseTitle: " + currentCourseTitle);
            System.out.println("Saving review for course: " + currentCourseMnemonic + " " + currentCourseNumber + " " + currentCourseTitle);
            System.out.println("Saving review for user: " + currentUser);

            int rating = Integer.parseInt(RatingTextArea.getText().trim());
            if (rating < 1 || rating > 5) {
                throw new NumberFormatException();
            }

            String content = ReviewContentField.getText().trim();
            Review newReview = new Review(
                    new Course(currentCourseMnemonic, currentCourseNumber, currentCourseTitle),
                    new User(currentUser, ""),
                    content,
                    new Timestamp(System.currentTimeMillis()),
                    rating
            );
            if (userReview == null) {
                // No existing review: Add a new review
                databaseService.saveReview(newReview);
            } else {
                // Existing review: Update the review
                databaseService.deleteReview(userReview);
                databaseService.saveReview(newReview);
            }
            // Update the userReview reference to the new review
            userReview = newReview;
            // Reload reviews and keep the input fields unchanged
            loadReviews();
            // Hide validation label
            ValidRatingLabel.setVisible(false);
        } catch (NumberFormatException e) {
            ValidRatingLabel.setVisible(true);
            ValidRatingLabel.setText("Invalid rating. Please enter integer 1 - 5!");
        }
    }


    @FXML
    void deleteReview(ActionEvent event) {
        if (userReview != null) {
            databaseService.deleteReview(currentUser, currentCourseMnemonic, currentCourseNumber, currentCourseTitle);
            userReview = null;
            RatingTextArea.clear();
            ReviewContentField.clear();
            loadReviews();
        }
    }

    @FXML
    void navigateBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE_PATH + "CourseSearch.fxml"));
            Parent root = loader.load();

            // Pass currentUser back to CourseSearchController
            CourseSearchController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) BackButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Course Search");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load CourseSearch.fxml");
        }
    }

    @FXML
    void navigateToAddCourse(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE_PATH + "AddCourse.fxml"));
            Parent root = loader.load();

            // Pass currentUser to AddCourseController
            AddCourseController controller = loader.getController();
            controller.initData(currentUser); // Pass the user

            // Navigate to the AddCourse scene
            Stage stage = (Stage) AddCourseButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Course");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load AddCourse.fxml");
        }
    }

    @FXML
    void navigateToLogin(ActionEvent event) {
        navigateToScene(event, FXML_BASE_PATH + "Log-inScreen.fxml", "Login");
    }

    @FXML
    void navigateToMyReviews(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_BASE_PATH + "MyReviews.fxml"));
            Parent root = loader.load();

            // Pass currentUser to MyReviewsController
            MyReviewsController controller = loader.getController();
            controller.initData(currentUser);

            // Set the scene
            Stage stage = (Stage) MyReviewsButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Reviews");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load MyReviews.fxml");
        }
    }

    private void navigateToScene(ActionEvent event, String fxmlFile, String title) {
        try {
            // Load the target FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Optionally, pass data to the next controller (if needed)
            // Example:
            // MyController controller = loader.getController();
            // controller.initData(data);

            // Set the new scene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load " + fxmlFile);
        }
    }
}
