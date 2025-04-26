package edu.virginia.sde.reviews.backend;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("SqlSourceToSinkFlow")
public class DatabaseService {

    private static final String DATABASE_FILE = "CRUDdy_Course_Reviews.db";
    private String databaseFileName = DATABASE_FILE;
    File databaseFile = new File(databaseFileName);
    private static Connection connection;
    private static DatabaseService instance;

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    protected Connection getConnection() {
        return connection;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean databaseExists() {
        return databaseFile.exists();
    }

    private void createDatabase() {
        try {
            boolean result = databaseFile.createNewFile();
            if (!result) throw new RuntimeException("Failed to create database file," +
                                                    " but an IOException was not thrown.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create database file: " + e.getMessage());
        }
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFileName);
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection to database: " + e.getMessage());
        }
    }

    private void createTablesIfNonExistent() {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY NOT NULL,"
                    + "password TEXT NOT NULL"
                    + ") STRICT;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS courses ("
                    + "mnemonic TEXT NOT NULL,"
                    + "number INTEGER NOT NULL,"
                    + "title TEXT NOT NULL,"
                    + "PRIMARY KEY (mnemonic, number, title)"
                    + ") STRICT;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS reviews ("
                    + "user TEXT NOT NULL,"
                    + "course_mnemonic TEXT NOT NULL,"
                    + "course_number INTEGER NOT NULL,"
                    + "course_title TEXT NOT NULL,"
                    + "rating INTEGER NOT NULL,"
                    + "content TEXT NOT NULL,"
                    + "timestamp TEXT NOT NULL,"
                    + "FOREIGN KEY (user) REFERENCES users(username),"
                    + "FOREIGN KEY (course_mnemonic, course_number, course_title) " +
                                                 "REFERENCES courses(mnemonic, number, title),"
                    + "PRIMARY KEY (user, course_mnemonic, course_number, course_title)"
                    + ") STRICT;");
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables: " + e.getMessage());
        }
    }

    private DatabaseService() {
        if (!databaseExists()) createDatabase();
        connectToDatabase();
        createTablesIfNonExistent();
    }

    protected DatabaseService(String databaseFileName) {
        this.databaseFileName = databaseFileName;
        if (!databaseExists()) createDatabase();
        connectToDatabase();
        createTablesIfNonExistent();
    }


    public Set<User> getUsers() {
        Set<User> users = new HashSet<>();
        try {
            ResultSet usersResultSet = connection.createStatement().executeQuery("SELECT * FROM users;");
            while (usersResultSet.next()) {
                users.add(new User(usersResultSet.getString("username"),
                        usersResultSet.getString("password")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get users: " + e.getMessage());
        }
        return users;
    }

    public Set<String> getDepartments() {
        Set<String> departments = new HashSet<>();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT DISTINCT mnemonic FROM courses;");
            while (resultSet.next()) {
                departments.add(resultSet.getString("mnemonic"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get departments: " + e.getMessage());
        }
        return departments;
    }


    public Set<Course> getCourses() {
        Set<Course> courses = new HashSet<>();
        try {
            ResultSet coursesResultSet = connection.createStatement().executeQuery("SELECT * FROM courses;");
            while (coursesResultSet.next()) {
                courses.add(new Course(coursesResultSet.getString("mnemonic"),
                        coursesResultSet.getInt("number"),
                        coursesResultSet.getString("title")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get courses: " + e.getMessage());
        }
        return courses;
    }

    public Set<Review> getReviews() {
        Set<Review> reviews = new HashSet<>();
        try {
            ResultSet reviewsResultSet = connection.createStatement().executeQuery("SELECT * FROM reviews;");
            while (reviewsResultSet.next()) {
                Optional<User> user = getUserByUsername(reviewsResultSet.getString("user"));
                if (user.isEmpty()) {
                    throw new RuntimeException("While getting reviews, a review was found with a user that does not exist.");
                }
                reviews.add(new Review(
                        new Course(reviewsResultSet.getString("course_mnemonic"),
                                reviewsResultSet.getInt("course_number"),
                                reviewsResultSet.getString("course_title")),
                        user.get(),
                        reviewsResultSet.getString("content"),
                        Timestamp.valueOf(reviewsResultSet.getString("timestamp")),
                        reviewsResultSet.getInt("rating")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get reviews: " + e.getMessage());
        }
        return reviews;
    }

    public Optional<User> getUserByUsername(String username) {
        try {
            ResultSet userResultSet = connection.createStatement().executeQuery("SELECT * FROM users WHERE username = '%s';"
                    .formatted(username));
            if (userResultSet.next()) {
                return Optional.of(new User(userResultSet.getString("username"),
                        userResultSet.getString("password")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user by username: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Set<Course>> getCoursesByDepartment(String departmentMnemonic) {
        try {
            Set<Course> courses = new HashSet<>();
            ResultSet coursesResultSet = connection.createStatement().executeQuery("SELECT * FROM courses WHERE mnemonic = '%s';"
                    .formatted(departmentMnemonic));
            while (coursesResultSet.next()) {
                courses.add(new Course(coursesResultSet.getString("mnemonic"),
                        coursesResultSet.getInt("number"),
                        coursesResultSet.getString("title")));
            }
            if (courses.isEmpty()) return Optional.empty();
            return Optional.of(courses);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get courses by department: " + e.getMessage());
        }
    }

    public Optional<Set<Course>> getCoursesByNumber(int number) {
        try {
            Set<Course> courses = new HashSet<>();
            ResultSet coursesResultSet = connection.createStatement().executeQuery("SELECT * FROM courses WHERE number = %d;"
                    .formatted(number));
            while (coursesResultSet.next()) {
                courses.add(new Course(coursesResultSet.getString("mnemonic"),
                        coursesResultSet.getInt("number"),
                        coursesResultSet.getString("title")));
            }
            if (courses.isEmpty()) return Optional.empty();
            return Optional.of(courses);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get courses by number: " + e.getMessage());
        }
    }

    public Optional<Set<Course>> getCoursesByTitle(String title) {
        try {
            Set<Course> courses = new HashSet<>();

            // Prepare the SQL query for case-insensitive substring search
            String query = "SELECT * FROM courses WHERE LOWER(title) LIKE LOWER(?)";

            // Use PreparedStatement to prevent SQL injection
            PreparedStatement stmt = connection.prepareStatement(query);

            // Set the parameter for the prepared statement
            stmt.setString(1, "%" + title + "%");  // Adding wildcards for substring search

            // Now you can execute the query and store the result in the existing coursesResultSet
            ResultSet coursesResultSet = stmt.executeQuery(); // No need to redefine if it's already declared

            // Iterate through the ResultSet and add the courses to the set
            while (coursesResultSet.next()) {
                courses.add(new Course(
                        coursesResultSet.getString("mnemonic"),
                        coursesResultSet.getInt("number"),
                        coursesResultSet.getString("title")
                ));
            }

            // If no courses were found, return an empty Optional
            if (courses.isEmpty()) {
                return Optional.empty();
            }

            // Return the set of courses wrapped in an Optional
            return Optional.of(courses);

        } catch (SQLException e) {
            // Handle SQL exceptions
            throw new RuntimeException("Failed to get courses by title: " + e.getMessage(), e);
        }
    }



    public Optional<Set<Course>> getCoursesByMnemonicAndNumber(String mnemonic, int number) {
        Set<Course> courses = new HashSet<>();
        try {
            ResultSet coursesResultSet = connection.createStatement().executeQuery("SELECT * FROM courses WHERE mnemonic = '%s' AND number = %d;"
                    .formatted(mnemonic, number));
            while (coursesResultSet.next()) {
                courses.add(new Course(coursesResultSet.getString("mnemonic"),
                        coursesResultSet.getInt("number"),
                        coursesResultSet.getString("title")));
            }
            if (courses.isEmpty()) return Optional.empty();
            return Optional.of(courses);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get courses by mnemonic and number: " + e.getMessage());
        }
    }

    public Optional<Course> getCourse(String mnemonic, int number, String courseTitle) {
        try {
            ResultSet courseResultSet = connection.createStatement().executeQuery("SELECT * FROM courses WHERE mnemonic = '%s' AND number = %d AND title = '%s';"
                    .formatted(mnemonic, number, courseTitle));
            if (courseResultSet.next()) {
                return Optional.of(new Course(courseResultSet.getString("mnemonic"),
                        courseResultSet.getInt("number"),
                        courseResultSet.getString("title")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get course: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Set<Review>> getReviewsByUser(User user) {
        try {
            Set<Review> reviews = new HashSet<>();
            ResultSet reviewsResultSet = connection.createStatement().executeQuery("SELECT * FROM reviews WHERE user = '%s';"
                    .formatted(user.getUsername()));
            while (reviewsResultSet.next()) {
                reviews.add(new Review(
                        new Course(reviewsResultSet.getString("course_mnemonic"),
                                reviewsResultSet.getInt("course_number"),
                                reviewsResultSet.getString("course_title")),
                        user,
                        reviewsResultSet.getString("content"),
                        Timestamp.valueOf(reviewsResultSet.getString("timestamp")),
                        reviewsResultSet.getInt("rating")));
            }
            if (reviews.isEmpty()) return Optional.empty();
            return Optional.of(reviews);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get reviews by user: " + e.getMessage());
        }
    }

    public Optional<Review> getReviewByUserAndCourse(User user, Course course) {
        try {
            String query = """
            SELECT * FROM reviews WHERE user = '%s' AND course_mnemonic = '%s'
            AND course_number = %d AND course_title = '%s';
        """.formatted(user.getUsername(), course.courseMnemonic(), course.courseNumber(), course.courseTitle());

            ResultSet resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()) {
                return Optional.of(new Review(
                        course,
                        user,
                        resultSet.getString("content"),
                        Timestamp.valueOf(resultSet.getString("timestamp")), // Convert timestamp to Java Timestamp
                        resultSet.getInt("rating")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get review by user and course: " + e.getMessage());
        }
        return Optional.empty();
    }


    public Optional<Set<Review>> getReviewsByCourse(Course course) {
        try {
            Set<Review> reviews = new HashSet<>();
            ResultSet reviewsResultSet = connection.createStatement().executeQuery("SELECT * FROM reviews WHERE course_mnemonic = '%s' AND course_number = %d AND course_title = '%s';"
                    .formatted(course.courseMnemonic(), course.courseNumber(), course.courseTitle()));
            while (reviewsResultSet.next()) {
                Optional<User> user = getUserByUsername(reviewsResultSet.getString("user"));
                if (user.isEmpty()) {
                    throw new RuntimeException("While getting reviews by course, a review was found with a user that does not exist.");
                }
                reviews.add(new Review(
                        course,
                        user.get(),
                        reviewsResultSet.getString("content"),
                        Timestamp.valueOf(reviewsResultSet.getString("timestamp")),
                        reviewsResultSet.getInt("rating")));
            }
            if (reviews.isEmpty()) return Optional.empty();
            return Optional.of(reviews);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get reviews by course: " + e.getMessage());
        }
    }

    public void saveUser(User user) {
        try {
            connection.createStatement().execute("INSERT INTO users (username, password) VALUES ('%s', '%s');"
                    .formatted(user.getUsername(), user.getPassword()));
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to save user: " + e.getMessage()
                                           + " and failed to rollback transaction: " + ex.getMessage());
            }
            throw new RuntimeException("Failed to save user: " + e.getMessage());
        }
    }

    public void saveCourse(Course course) {
        try {
            connection.createStatement().execute(("INSERT INTO courses (mnemonic, number, title) " +
                                                  "VALUES ('%s', %d, '%s');")
                    .formatted(course.courseMnemonic(), course.courseNumber(), course.courseTitle()));
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to save course: " + e.getMessage()
                                           + " and failed to rollback transaction: " + ex.getMessage());
            }
            throw new RuntimeException("Failed to save course: " + e.getMessage());
        }
    }

    public void saveReview(Review review) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = review.getTimestamp();
        sdf.format(timestamp);
        try {
            String query = """
            INSERT INTO reviews (user, course_mnemonic, course_number, course_title, rating, content, timestamp)
            VALUES ('%s', '%s', %d, '%s', %d, '%s', '%s');
            """.formatted(review.getUser().getUsername(), review.getCourse().courseMnemonic(),
                          review.getCourse().courseNumber(), review.getCourse().courseTitle(),
                          review.getRating(), review.getReviewContent(), timestamp);
            connection.createStatement().execute(query);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to save review: " + e.getMessage() + " and failed to rollback: " + ex.getMessage());
            }
            throw new RuntimeException("Failed to save review: " + e.getMessage());
        }
    }

    public void deleteReview(String username, String courseMnemonic, int courseNumber, String courseTitle) {
        try {
            connection.createStatement().execute(("DELETE FROM reviews WHERE user = '%s' AND course_mnemonic = '%s' " +
                                                  "AND course_number = %d AND course_title = '%s';")
                    .formatted(username, courseMnemonic, courseNumber, courseTitle));
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to delete review: " + e.getMessage()
                                           + " and failed to rollback transaction: " + ex.getMessage());
            }
            throw new RuntimeException("Failed to delete review: " + e.getMessage());
        }
    }

    public void deleteReview(Review review) {
        deleteReview(review.getUser().getUsername(), review.getCourse().courseMnemonic(),
                     review.getCourse().courseNumber(), review.getCourse().courseTitle());
    }

    @SuppressWarnings("SqlWithoutWhere")
    public void clearTables() {
        try {
            connection.createStatement().execute("DELETE FROM reviews;");
            connection.createStatement().execute("DELETE FROM users;");
            connection.createStatement().execute("DELETE FROM courses;");
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear tables: " + e.getMessage());
        }
    }
}
