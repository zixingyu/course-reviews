package edu.virginia.sde.reviews.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginServiceTest {
    private LoginService loginService;
    @Mock
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new LoginService(databaseService);
    }

    @Test
    void login_successful() {
        String JD_username = "JohnDoe2028";
        String JD_password = "123456JD";
        User JD_user  = new User(JD_username,JD_password);

        when(databaseService.getUserByUsername(JD_username)).thenReturn(Optional.of(JD_user));
        String result = loginService.login(JD_username,JD_password);

        assertEquals("Login successful!", result, "The login message should indicate success.");
        verify(databaseService).getUserByUsername(JD_username);
    }

    @Test
    void login_UsernameNotFound() {
        String wrong_username = "JohnDoe";
        String JD_username = "JohnDoe2028";
        String JD_password = "123456JD";

        when(databaseService.getUserByUsername(JD_username)).thenReturn(null);
        String result = loginService.login(wrong_username,JD_password);

        assertEquals("Username not found.", result, "The login message should indicate user not found.");
    }

    @Test
    void login_IncorrectPassowrd() {
        String wrong_password = "000000JD";
        String JD_username = "JohnDoe2028";
        String JD_password = "123456JD";
        User JD_user  = new User(JD_username,JD_password);

        when(databaseService.getUserByUsername(JD_username)).thenReturn(Optional.of(JD_user));
        String result = loginService.login(JD_username, wrong_password);

        assertEquals("Incorrect password.", result, "The login message should indicate incorrect password.");
    }

    @Test
    void createUser_Successful() {
        // Scenario: Valid username and password
        String username = "newUser";
        String password = "validPassword";

        // Mock behavior: no existing user with the username
        when(databaseService.getUserByUsername(username)).thenReturn(Optional.empty());

        // When
        String result = loginService.createUser(username, password);

        // Then
        assertEquals("User created successfully. Please log in.", result, "The success message should indicate the user was created.");
        verify(databaseService).getUserByUsername(username);
        verify(databaseService).saveUser(new User(username, password)); // Matching works now
    }

    @Test
    void createUser_EmptyUsername() {
        String emptyUsername = "";
        String password = "validPassword";

        String result = loginService.createUser(emptyUsername, password);

        assertEquals("Username cannot be empty.", result, "The error message should indicate that the username is empty.");
    }

    @Test
    void createUser_NullUsername() {
        String emptyUsername = null;
        String password = "validPassword";

        String result = loginService.createUser(emptyUsername, password);

        assertEquals("Username cannot be empty.", result, "The error message should indicate that the username is empty.");
    }

    @Test
    void createUser_UsernameAlreadyExists() {
        String existingUsername = "testUser";
        String password = "validPassword";
        User existingUser = new User(existingUsername, password);

        when(databaseService.getUserByUsername(existingUsername)).thenReturn(Optional.of(existingUser));

        String result = loginService.createUser(existingUsername, password);

        assertEquals("Username already exists.", result, "The error message should indicate that the username already exists.");
        verify(databaseService).getUserByUsername(existingUsername);
    }

    @Test
    void createUser_PasswordTooShort() {
        String username = "newUser";
        String shortPassword = "short";

        when(databaseService.getUserByUsername(username)).thenReturn(Optional.empty());

        String result = loginService.createUser(username, shortPassword);

        assertEquals("Password must be at least 8 characters long.", result, "The error message should indicate the password is too short.");
        verify(databaseService).getUserByUsername(username);
    }

    @Test
    void createUser_NullPassword() {
        String username = "newUser";
        String nullPassword = null;

        when(databaseService.getUserByUsername(username)).thenReturn(Optional.empty());

        String result = loginService.createUser(username, nullPassword);

        assertEquals("Password must be at least 8 characters long.", result, "The error message should indicate the password is too short.");
        verify(databaseService).getUserByUsername(username);
    }
}
