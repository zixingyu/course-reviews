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
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MyReviewsController {

    @FXML
    private Label userReviewLabel;

    @FXML
    private Button addCourseButton;

    @FXML
    private Button backButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button myReviewsButton;

    @FXML
    private ListView<String> reviewListView;

    private DatabaseService databaseService = DatabaseService.getInstance();
    private String currentUser;
    private ObservableList<String> reviewItems = FXCollections.observableArrayList();

    // Initialize the controller with the current user's information
    public void initData(String user) {
        this.currentUser = user;
        // System.out.println("MyReviewsController: currentUser = " + currentUser);
        loadUserReviews();
    }

    @FXML
    public void initialize() {
        reviewListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Single-click navigation
                String selectedItem = reviewListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    System.out.println("Navigating to review details for: " + selectedItem);
                    navigateToReviewDetails(selectedItem);
                }
            }
        });
    }

    private void loadUserReviews() {
        reviewItems.clear();

        try {
            // Fetch reviews for the logged-in user
            Optional<Set<Review>> userReviews = databaseService.getReviewsByUser(new User(currentUser, ""));
            userReviews.ifPresent(reviews -> reviewItems.addAll(
                    reviews.stream().map(this::formatReview).collect(Collectors.toList())
            ));

            // Populate the ListView
            reviewListView.setItems(reviewItems);

            if (reviewItems.isEmpty()) {
                System.out.println("No reviews found for user: " + currentUser);
            }
        } catch (Exception e) {
            // Handle potential database or state errors
            System.err.println("Error loading user reviews: " + e.getMessage());
            showError("Failed to load reviews. Please try again.");
        }
    }

    private String formatReview(Review review) {
        return String.format("%s %d: %s\nRating: %d\n%s",
                review.getCourse().courseMnemonic(),
                review.getCourse().courseNumber(),
                review.getCourse().courseTitle(),
                review.getRating(),
                review.getReviewContent().isEmpty() ? "No comment" : review.getReviewContent());
    }

private void navigateToReviewDetails(String selectedReview) {
    try {
        // Find the review from the formatted string
        Review review = findReviewFromFormattedString(selectedReview);
        if (review == null) {
            showError("Failed to find the selected review.");
            return;
        }

        // Load the Reviews scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseReview.fxml"));
        Parent root = loader.load();

        // Pass data to ReviewsController
        ReviewsController reviewsController = loader.getController();
        reviewsController.initData(
                currentUser,
                review.getCourse().courseMnemonic(),
                review.getCourse().courseNumber(),
                review.getCourse().courseTitle()
        );

        // Navigate to the Reviews scene
        Stage stage = (Stage) reviewListView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Course Review");
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        showError("Failed to load the review details scene.");
    } catch (Exception e) {
        e.printStackTrace();
        showError("Unexpected error occurred while navigating to review details.");
    }
}

private Review findReviewFromFormattedString(String formattedString) {
    try {
        String[] lines = formattedString.split("\n");
        String[] firstLine = lines[0].split(":");
        String[] mnemonicAndNumber = firstLine[0].split(" ");
        String mnemonic = mnemonicAndNumber[0];
        int number = Integer.parseInt(mnemonicAndNumber[1]);
        String title = firstLine[1].trim();

        return databaseService.getReviewByUserAndCourse(
                new User(currentUser, ""),
                new Course(mnemonic, number, title)
        ).orElseThrow(() -> new RuntimeException("Review not found in the database."));
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error parsing review string: " + formattedString, e);
    }
}


    @FXML
    void navigateBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
            Parent root = loader.load();

            // Pass currentUser back to CourseSearchController
            CourseSearchController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Course Search");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to navigate back to Course Search.");
        }
    }

    @FXML
    void navigateToAddCourse(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/AddCourse.fxml"));
            Parent root = loader.load();

            // Pass currentUser to AddCourseController
            AddCourseController controller = loader.getController();
            controller.initData(currentUser);

            Stage stage = (Stage) addCourseButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Course");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load AddCourse.fxml");
        }
    }


    @FXML
    void navigateToLogin(ActionEvent event) {
        navigateToScene("/edu/virginia/sde/reviews/Log-inScreen.fxml", "Login");
    }

@FXML
void navigateToMyReviews(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/MyReviews.fxml"));
        Parent root = loader.load();

        // Pass currentUser to MyReviewsController
        MyReviewsController controller = loader.getController();
        controller.initData(currentUser);

        // Set the scene
        Stage stage = (Stage) myReviewsButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("My Reviews");
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Failed to load MyReviews.fxml");
    }
}


    private void navigateToScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to navigate to " + title);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}
