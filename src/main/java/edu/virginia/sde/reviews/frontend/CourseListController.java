package edu.virginia.sde.reviews.frontend;

import edu.virginia.sde.reviews.backend.Course;
import edu.virginia.sde.reviews.backend.DatabaseService;
import edu.virginia.sde.reviews.backend.Review;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CourseListController {

    @FXML
    private VBox courseCardList; // VBox to hold the list of course cards

    @FXML
    private Button backButton;

    private String currentUser;

    public void setCurrentUser(String user) {
        this.currentUser = user;
    }


    /**
     * Populates the course list dynamically.
     *
     * @param courses The set of courses to display
     */
    public void setCourses(List<Course> courses) {
        // Clear any existing course cards
        courseCardList.getChildren().clear();
        courseCardList.setAlignment(Pos.CENTER);  // Centers the course cards
        courseCardList.setSpacing(10);            // Adds space between cards
        courseCardList.setPadding(new Insets(20, 0, 20, 0)); // Adds space from the top and bottom


        // Add a card for each course
        for (Course course : courses) {
            addCourseCard(course);
        }
    }

    private void addCourseCard(Course course) {
        // Create a new AnchorPane for each course card
        AnchorPane courseCard = new AnchorPane();
        courseCard.setPrefHeight(72.0);
        courseCard.setPrefWidth(720.0);

        AnchorPane.setLeftAnchor(courseCard, 0.0);    // Align to the left edge
        AnchorPane.setRightAnchor(courseCard, 0.0);   // Align to the right edge


        // Create a Rectangle for the background (optional, for additional visual styling)
        Rectangle background = new Rectangle(720.0, 72.0);
        background.setArcHeight(50.0);
        background.setArcWidth(50.0);
        background.setStyle("-fx-fill: #b0afba29;");
        background.setX(90);  // Align left of AnchorPane
        background.setY(0);  // Align top of AnchorPane
        courseCard.getChildren().add(background); // Add background to the course card

        // Course mnemonic (e.g., "MEMO")
        Label mnemonicLabel = new Label(course.courseMnemonic());
        mnemonicLabel.setLayoutX(110.0);
        mnemonicLabel.setLayoutY(21.0);
        mnemonicLabel.setStyle("-fx-font-family: 'Avenir Heavy'; -fx-font-size: 20;");

        // Course number
        Label courseNumberLabel = new Label(course.courseNumber() + ": ");
        courseNumberLabel.setLayoutX(180.0);
        courseNumberLabel.setLayoutY(21.0);
        courseNumberLabel.setStyle("-fx-font-family: 'Avenir Heavy'; -fx-font-size: 20;");

        // Course title
        Label titleLabel = new Label(course.courseTitle());
        titleLabel.setLayoutX(250.0);
        titleLabel.setLayoutY(21.0);
        titleLabel.setStyle("-fx-font-family: 'Avenir Heavy'; -fx-font-size: 20;");

        // Fetch reviews for the course and calculate the average rating
        Set<Review> reviews = DatabaseService.getInstance().getReviewsByCourse(course).orElse(Set.of());
        double averageRating = 0.0;

        if (!reviews.isEmpty()) {
            double totalRating = 0.0;
            for (Review review : reviews) {
                totalRating += review.getRating();
            }
            averageRating = totalRating / reviews.size();
        }

        // Format the rating to two decimal places, or show " " (blank) if no reviews are available
        String formattedRating = (reviews.isEmpty()) ? " " : String.format("%.2f", averageRating);

        // Course rating label
        Label ratingLabel = new Label(formattedRating);
        ratingLabel.setLayoutX(750.0);
        ratingLabel.setLayoutY(21.0);
        ratingLabel.setStyle("-fx-font-size: 20;");

        // Make the card clickable
        courseCard.setOnMouseClicked(event -> {
            // Logic to navigate to the course review screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseReview.fxml"));
            try {
                Parent courseReviewParent = loader.load();
                ReviewsController reviewsController = loader.getController();
                reviewsController.setCourse(course); // Pass the selected course to the review controller
                reviewsController.initData(currentUser, course.courseMnemonic(), course.courseNumber(), course.courseTitle());
                // Transition to the new scene
                Stage stage = (Stage) courseCard.getScene().getWindow();
                stage.setScene(new Scene(courseReviewParent));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Add components to the course card
        courseCard.getChildren().addAll(mnemonicLabel, courseNumberLabel, titleLabel, ratingLabel);

        // Add the course card to the VBox
        courseCardList.getChildren().add(courseCard);
    }

    @FXML
    private void handleBackButtonAction() {
        try {
            // Load the course search scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
            Parent courseSearchParent = loader.load();
            // Retrieve the CourseSearchController and set the current user
            CourseSearchController courseSearchController = loader.getController();
            courseSearchController.setCurrentUser(currentUser);
            // Transition back to the course search scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(courseSearchParent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}