package edu.virginia.sde.reviews.backend;

import java.util.Optional;

public class LoginService {
    /*** This class interacts with the database and works as a validator for checking log-in or
     * creating new users.**/
    private DatabaseService databaseService; // Handles interaction with the database.

    public LoginService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    // Log in an existing user.
    public String login(String username, String password) {
        Optional<User> user = databaseService.getUserByUsername(username);
        if (user.isEmpty()) {
            return "Username not found.";
        }
        if (!user.get().getPassword().equals(password)) {
            return "Incorrect password.";
        }
        return "Login successful!";
    }


    // Create a new user.
    public String createUser(String username, String password) {
        if (username == null || username.isEmpty()) {
            return "Username cannot be empty.";
        }
        if (databaseService.getUserByUsername(username).isPresent()) {
            return "Username already exists.";
        }
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        User newUser = new User(username, password);
        databaseService.saveUser(newUser); // Save the new user in the database.
        return "User created successfully. Please log in.";
    }
}
