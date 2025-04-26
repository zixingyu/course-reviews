package edu.virginia.sde.reviews.frontend;

import edu.virginia.sde.reviews.backend.LoginService;
import edu.virginia.sde.reviews.backend.DatabaseService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Label instructionLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Button exitButton;

    private LoginService loginService;

    private DatabaseService databaseService;

    @FXML
    public void initialize() {
        System.out.println("LoginController initialized");
        databaseService = DatabaseService.getInstance();
        loginService = new LoginService(databaseService);

        // Set button actions
        loginButton.setOnAction(event -> handleLogin());
        signUpButton.setOnAction(event -> handleSignUp());
        exitButton.setOnMouseClicked(event -> exitApplication());
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            instructionLabel.setText("Please enter both username and password.");
            instructionLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String result = loginService.login(username, password);

        if (result.equals("Login successful!")) {
            System.out.println("Login successfull...");
            instructionLabel.setText(result); // Show success message
            instructionLabel.setStyle("-fx-text-fill: green;");
            // Immediately navigate to CourseSearch
            navigateToCourseSearch();
        } else {
            instructionLabel.setText(result); // Show login failure message
            instructionLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            instructionLabel.setText("Please enter both username and password to sign up.");
            instructionLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String result = loginService.createUser(username, password);
        instructionLabel.setText(result); // Show signup result
        if(result.contains("successfully")){
            instructionLabel.setStyle("-fx-text-fill: green;");
        }
        else{
            instructionLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void exitApplication() {
        // Close the application
        Stage stage = (Stage) exitButton.getScene().getWindow();
        databaseService.closeConnection();
        stage.close();
    }

    private void navigateToCourseSearch() {
        try {
            // Create a PauseTransition to delay the scene change by 0.5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5)); // 0.5-second delay

            pause.setOnFinished(event -> {
                try {
                    // Load the CourseSearch scene after the delay
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/CourseSearch.fxml"));
                    Parent courseSearchParent = loader.load();
                    Scene courseSearchScene = new Scene(courseSearchParent);

                    // Pass the currentUser to CourseSearchController
                    CourseSearchController controller = loader.getController();
                    controller.setCurrentUser(usernameField.getText());

                    // Get the current stage and set the new scene
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(courseSearchScene);
                    stage.show();
                    System.out.println("Scene successfully changed!");
                } catch (IOException e) {
                    System.err.println("Failed to load CourseSearch.fxml");
                    e.printStackTrace();
                }
            });

            // Start the PauseTransition
            pause.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
