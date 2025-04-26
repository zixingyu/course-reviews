package edu.virginia.sde.reviews.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class DatabaseServiceTest {

    private static final String TEST_DATABASE_FILE = "test_database/test_database.db";
    private DatabaseService databaseService;
    private Connection connection;

    @BeforeEach
    void setUp() {
        databaseService = new DatabaseService(TEST_DATABASE_FILE);
        connection = databaseService.getConnection();
    }

    @AfterEach
    void tearDown() {
        databaseService.clearTables();
        databaseService.closeConnection();
    }

    private void checkCreatedTables() {
        DatabaseMetaData metaData = assertDoesNotThrow(() -> connection.getMetaData());
        assertNotNull(metaData);
        ResultSet tables = assertDoesNotThrow(() -> metaData.getTables(null, null, null, new String[]{"TABLE"}));
        assertNotNull(tables);
        String[] tableNames = new String[3];
        for (int i = 1; i <= 3; i++) {
            assertDoesNotThrow(() -> assertTrue(tables.next()));
            tableNames[i - 1] = assertDoesNotThrow(() -> tables.getString("TABLE_NAME"));
        }
        assertTrue(Arrays.asList(tableNames).contains("users"));
        assertTrue(Arrays.asList(tableNames).contains("courses"));
        assertTrue(Arrays.asList(tableNames).contains("reviews"));
    }

    @Test
    void testConstructor() {
        assertNotNull(connection);
        assertNotNull(databaseService);
        checkCreatedTables();
    }

    @Test
    void getUsers() {
        assertEquals(0, databaseService.getUsers().size());
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        when(user1.getUsername()).thenReturn("user1");
        when(user2.getUsername()).thenReturn("user2");
        when(user1.getPassword()).thenReturn("password1");
        when(user2.getPassword()).thenReturn("password2");
        databaseService.saveUser(user1);
        databaseService.saveUser(user2);
        Set<User> users = databaseService.getUsers();
        assertEquals(2, users.size());
        for (User user : users) {
            assertTrue(user.getUsername().equals("user1") &&  user.getPassword().equals("password1")
                        || user.getUsername().equals("user2") && user.getPassword().equals("password2"));
        }
    }

    @Test
    void getCourses() {
        assertEquals(0, databaseService.getCourses().size());
        Course course1 = mock(Course.class);
        Course course2 = mock(Course.class);
        when(course1.courseMnemonic()).thenReturn("CS");
        when(course1.courseNumber()).thenReturn(3140);
        when(course1.courseTitle()).thenReturn("Software Development Essentials");
        when(course2.courseMnemonic()).thenReturn("CS");
        when(course2.courseNumber()).thenReturn(2150);
        when(course2.courseTitle()).thenReturn("Program and Data Representation");
        databaseService.saveCourse(course1);
        databaseService.saveCourse(course2);
        Set<Course> courses = databaseService.getCourses();
        assertEquals(2, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("CS") && course.courseNumber() == 3140 && course.courseTitle().equals("Software Development Essentials")
                        || course.courseMnemonic().equals("CS") && course.courseNumber() == 2150 && course.courseTitle().equals("Program and Data Representation"));
        }
    }

    @Test
    void getReviews() {
        assertEquals(0, databaseService.getReviews().size());
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);
        Course course1 = mock(Course.class);
        Course course2 = mock(Course.class);
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        when(review1.getTimestamp()).thenReturn(timestamp);
        when(review2.getTimestamp()).thenReturn(timestamp);
        when(course1.courseMnemonic()).thenReturn("CS");
        when(course1.courseNumber()).thenReturn(3140);
        when(course1.courseTitle()).thenReturn("Software Development Essentials");
        when(course2.courseMnemonic()).thenReturn("CS");
        when(course2.courseNumber()).thenReturn(2150);
        when(course2.courseTitle()).thenReturn("Program and Data Representation");
        when(user1.getUsername()).thenReturn("user1");
        when(user1.getPassword()).thenReturn("password1");
        when(user2.getUsername()).thenReturn("user2");
        when(user2.getPassword()).thenReturn("password2");
        when(review1.getRating()).thenReturn(5);
        when(review1.getReviewContent()).thenReturn("Great course!");
        when(review1.getCourse()).thenReturn(course1);
        when(review1.getUser()).thenReturn(user1);
        when(review2.getRating()).thenReturn(1);
        when(review2.getReviewContent()).thenReturn("Bad course!");
        when(review2.getCourse()).thenReturn(course2);
        when(review2.getUser()).thenReturn(user2);
        databaseService.saveCourse(course1);
        databaseService.saveCourse(course2);
        databaseService.saveUser(user1);
        databaseService.saveUser(user2);
        databaseService.saveReview(review1);
        databaseService.saveReview(review2);
        Set<Review> reviews = databaseService.getReviews();
        assertEquals(2, reviews.size());
        for (Review review : reviews) {
            assertTrue(review.getUser().getUsername().equals("user1")
                    && review.getUser().getPassword().equals("password1")
                    && review.getCourse().courseMnemonic().equals("CS")
                    && review.getCourse().courseNumber() == 3140
                    && review.getCourse().courseTitle().equals("Software Development Essentials")
                    && review.getRating() == 5 && review.getReviewContent().equals("Great course!")
                    || review.getUser().getUsername().equals("user2")
                    && review.getUser().getPassword().equals("password2")
                    && review.getCourse().courseMnemonic().equals("CS")
                    && review.getCourse().courseNumber() == 2150
                    && review.getCourse().courseTitle().equals("Program and Data Representation")
                    && review.getRating() == 1 && review.getReviewContent().equals("Bad course!"));
        }
    }

    void getUserSetup() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);
        User targetUser = mock(User.class);
        User falseTargetUser = mock(User.class);
        when(user1.getUsername()).thenReturn("user1");
        when(user2.getUsername()).thenReturn("user2");
        when(user3.getUsername()).thenReturn("user3");
        when(targetUser.getUsername()).thenReturn("targetUser");
        when(falseTargetUser.getUsername()).thenReturn("falseTargetUser");
        when(user1.getPassword()).thenReturn("password1");
        when(user2.getPassword()).thenReturn("password2");
        when(user3.getPassword()).thenReturn("password3");
        when(targetUser.getPassword()).thenReturn("targetPassword");
        when(falseTargetUser.getPassword()).thenReturn("falseTargetPassword");
        databaseService.saveUser(user1);
        databaseService.saveUser(user2);
        databaseService.saveUser(user3);
        databaseService.saveUser(targetUser);
        databaseService.saveUser(falseTargetUser);
    }

    @Test
    void getUserByUsername() {
        getUserSetup();
        Optional<User> userOptional = databaseService.getUserByUsername("targetUser");
        assertTrue(userOptional.isPresent());
        User user = userOptional.get();
        assertEquals("targetUser", user.getUsername());
        assertEquals("targetPassword", user.getPassword());
    }

    @Test
    void getUserByUsernameNotPresent() {
        getUserSetup();
        Optional<User> userOptional = databaseService.getUserByUsername("user");
        assertTrue(userOptional.isEmpty());
    }

    void getCourseSetup() {
        Course course1 = mock(Course.class);
        Course course2 = mock(Course.class);
        Course course3 = mock(Course.class);
        Course course4 = mock(Course.class);
        Course course5 = mock(Course.class);
        Course course6 = mock(Course.class);
        Course course7 = mock(Course.class);
        Course targetCourse = mock(Course.class);
        Course falseTargetCourse = mock(Course.class);
        when(course1.courseMnemonic()).thenReturn("CS");
        when(course1.courseNumber()).thenReturn(3140);
        when(course1.courseTitle()).thenReturn("Software Development Essentials");
        when(course2.courseMnemonic()).thenReturn("CS");
        when(course2.courseNumber()).thenReturn(2150);
        when(course2.courseTitle()).thenReturn("Program and Data Representation");
        when(course3.courseMnemonic()).thenReturn("BIOL");
        when(course3.courseNumber()).thenReturn(3030);
        when(course3.courseTitle()).thenReturn("Biochemistry");
        when(course4.courseMnemonic()).thenReturn("CS");
        when(course4.courseNumber()).thenReturn(1501);
        when(course4.courseTitle()).thenReturn("Bot Basics: Examining Automata");
        when(course5.courseMnemonic()).thenReturn("CS");
        when(course5.courseNumber()).thenReturn(1501);
        when(course5.courseTitle()).thenReturn("Intro to Hacktivism: Hacking for a Cause");
        when(course6.courseMnemonic()).thenReturn("RELC");
        when(course6.courseNumber()).thenReturn(1210);
        when(course6.courseTitle()).thenReturn("Hebrew Bible/Old Testament");
        when(course7.courseMnemonic()).thenReturn("RELJ");
        when(course7.courseNumber()).thenReturn(1210);
        when(course7.courseTitle()).thenReturn("Hebrew Bible/Old Testament");
        when(targetCourse.courseMnemonic()).thenReturn("CS");
        when(targetCourse.courseNumber()).thenReturn(2100);
        when(targetCourse.courseTitle()).thenReturn("Data Structures and Algorithms");
        when(falseTargetCourse.courseMnemonic()).thenReturn("CS");
        when(falseTargetCourse.courseNumber()).thenReturn(3100);
        when(falseTargetCourse.courseTitle()).thenReturn("Data Structures and Algorithms 2");
        databaseService.saveCourse(course1);
        databaseService.saveCourse(course2);
        databaseService.saveCourse(course3);
        databaseService.saveCourse(course4);
        databaseService.saveCourse(course5);
        databaseService.saveCourse(course6);
        databaseService.saveCourse(course7);
        databaseService.saveCourse(targetCourse);
        databaseService.saveCourse(falseTargetCourse);
    }

    @Test
    void getCoursesByDepartment() {
        getCourseSetup();
        Optional<Set<Course>> coursesOptional = databaseService.getCoursesByDepartment("CS");
        assertTrue(coursesOptional.isPresent());
        Set<Course> courses = coursesOptional.get();
        assertEquals(6, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 3140
                    && course.courseTitle().equals("Software Development Essentials")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 2150
                    && course.courseTitle().equals("Program and Data Representation")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 2100
                    && course.courseTitle().equals("Data Structures and Algorithms")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 3100
                    && course.courseTitle().equals("Data Structures and Algorithms 2")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 1501
                    && course.courseTitle().equals("Bot Basics: Examining Automata")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 1501
                    && course.courseTitle().equals("Intro to Hacktivism: Hacking for a Cause"));
        }
    }

    @Test
    void getCoursesByDepartmentNotPresent() {
        getCourseSetup();
        Optional<Set<Course>> coursesOptional = databaseService.getCoursesByDepartment("MATH");
        assertTrue(coursesOptional.isEmpty());
    }

    @Test
    void getCoursesByNumberOneResult() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByNumber(3140);
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(1, courses.size());
        Course course = courses.iterator().next();
        assertEquals("CS", course.courseMnemonic());
        assertEquals(3140, course.courseNumber());
        assertEquals("Software Development Essentials", course.courseTitle());
    }

    @Test
    void getCoursesByNumberTwoResults() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByNumber(1210);
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(2, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("RELC")
                    && course.courseNumber() == 1210
                    && course.courseTitle().equals("Hebrew Bible/Old Testament")
                    || course.courseMnemonic().equals("RELJ")
                    && course.courseNumber() == 1210
                    && course.courseTitle().equals("Hebrew Bible/Old Testament"));
        }
    }

    @Test
    void getCoursesByNumberNotPresent() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByNumber(4710);
        assertTrue(courseOptional.isEmpty());
    }

    @Test
    void getCoursesByMnemonicAndNumberOneResult() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByMnemonicAndNumber("CS", 2100);
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(1, courses.size());
        Course course = courses.iterator().next();
        assertEquals("CS", course.courseMnemonic());
        assertEquals(2100, course.courseNumber());
        assertEquals("Data Structures and Algorithms", course.courseTitle());
    }

    @Test
    void getCoursesByMnemonicAndNumberTwoResults() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByMnemonicAndNumber("CS", 1501);
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(2, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 1501
                    && course.courseTitle().equals("Bot Basics: Examining Automata")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 1501
                    && course.courseTitle().equals("Intro to Hacktivism: Hacking for a Cause"));
        }
    }

    @Test
    void getCoursesByMnemonicAndNumberNotPresent() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByMnemonicAndNumber("CS", 2101);
        assertTrue(courseOptional.isEmpty());
    }

    @Test
    void getCoursesByTitleOneResult() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByTitle("Bot Basics: Examining Automata");
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(1, courses.size());
        Course course = courses.iterator().next();
        assertEquals("CS", course.courseMnemonic());
        assertEquals(1501, course.courseNumber());
        assertEquals("Bot Basics: Examining Automata", course.courseTitle());
    }

    @Test
    void getCoursesByTitleTwoResults() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByTitle("Hebrew Bible/Old Testament");
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(2, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("RELC")
                    && course.courseNumber() == 1210
                    && course.courseTitle().equals("Hebrew Bible/Old Testament")
                    || course.courseMnemonic().equals("RELJ")
                    && course.courseNumber() == 1210
                    && course.courseTitle().equals("Hebrew Bible/Old Testament"));
        }
    }

    @Test
    void getCoursesByTitleNotPresent() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByTitle("Computer Systems and Organization 2");
        assertTrue(courseOptional.isEmpty());
    }

    @Test
    void getCoursesByTitle_Substring() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByTitle("Data Structures");
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(2, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 2100
                    && course.courseTitle().equals("Data Structures and Algorithms")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 3100
                    && course.courseTitle().equals("Data Structures and Algorithms 2"));
        }
    }

    @Test
    void getCoursesByTitle_Substring_DifferentCase() {
        getCourseSetup();
        Optional<Set<Course>> courseOptional = databaseService.getCoursesByTitle("data structures");
        assertTrue(courseOptional.isPresent());
        Set<Course> courses = courseOptional.get();
        assertEquals(2, courses.size());
        for (Course course : courses) {
            assertTrue(course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 2100
                    && course.courseTitle().equals("Data Structures and Algorithms")
                    || course.courseMnemonic().equals("CS")
                    && course.courseNumber() == 3100
                    && course.courseTitle().equals("Data Structures and Algorithms 2"));
        }
    }

    @Test
    void getCourse() {
        getCourseSetup();
        Optional<Course> courseOptional = databaseService.getCourse("CS", 2100, "Data Structures and Algorithms");
        assertTrue(courseOptional.isPresent());
        Course course = courseOptional.get();
        assertEquals("CS", course.courseMnemonic());
        assertEquals(2100, course.courseNumber());
        assertEquals("Data Structures and Algorithms", course.courseTitle());
    }

    @Test
    void getCourseNotPresent() {
        getCourseSetup();
        Optional<Course> courseOptional = databaseService.getCourse("CS", 2101, "Data Structures and Algorithms");
        assertTrue(courseOptional.isEmpty());
    }

    void reviewsSetup() {
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);
        Review review3 = mock(Review.class);
        Review review4 = mock(Review.class);
        Review review5 = mock(Review.class);
        Review review6 = mock(Review.class);
        Review review7 = mock(Review.class);
        Review review8 = mock(Review.class);
        Review review9 = mock(Review.class);
        Course course1 = mock(Course.class);
        Course course2 = mock(Course.class);
        Course course3 = mock(Course.class);
        Course course4 = mock(Course.class);
        Course course5 = mock(Course.class);
        Course course6 = mock(Course.class);
        Course course7 = mock(Course.class);
        Course course8 = mock(Course.class);
        Course course9 = mock(Course.class);
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);
        User user4 = mock(User.class);
        User user5 = mock(User.class);
        User user6 = mock(User.class);
        User user7 = mock(User.class);
        User user8 = mock(User.class);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        when(review1.getTimestamp()).thenReturn(timestamp);
        when(review2.getTimestamp()).thenReturn(timestamp);
        when(review3.getTimestamp()).thenReturn(timestamp);
        when(review4.getTimestamp()).thenReturn(timestamp);
        when(review5.getTimestamp()).thenReturn(timestamp);
        when(review6.getTimestamp()).thenReturn(timestamp);
        when(review7.getTimestamp()).thenReturn(timestamp);
        when(review8.getTimestamp()).thenReturn(timestamp);
        when(review9.getTimestamp()).thenReturn(timestamp);
        when(course1.courseMnemonic()).thenReturn("CS");
        when(course1.courseNumber()).thenReturn(3140);
        when(course1.courseTitle()).thenReturn("Software Development Essentials");
        when(course2.courseMnemonic()).thenReturn("CS");
        when(course2.courseNumber()).thenReturn(2150);
        when(course2.courseTitle()).thenReturn("Program and Data Representation");
        when(course3.courseMnemonic()).thenReturn("BIOL");
        when(course3.courseNumber()).thenReturn(3030);
        when(course3.courseTitle()).thenReturn("Biochemistry");
        when(course4.courseMnemonic()).thenReturn("CS");
        when(course4.courseNumber()).thenReturn(1501);
        when(course4.courseTitle()).thenReturn("Bot Basics: Examining Automata");
        when(course5.courseMnemonic()).thenReturn("CS");
        when(course5.courseNumber()).thenReturn(1501);
        when(course5.courseTitle()).thenReturn("Intro to Hacktivism: Hacking for a Cause");
        when(course6.courseMnemonic()).thenReturn("RELC");
        when(course6.courseNumber()).thenReturn(1210);
        when(course6.courseTitle()).thenReturn("Hebrew Bible/Old Testament");
        when(course7.courseMnemonic()).thenReturn("RELJ");
        when(course7.courseNumber()).thenReturn(1210);
        when(course7.courseTitle()).thenReturn("Hebrew Bible/Old Testament");
        when(course8.courseMnemonic()).thenReturn("CS");
        when(course8.courseNumber()).thenReturn(2100);
        when(course8.courseTitle()).thenReturn("Data Structures and Algorithms");
        when(course9.courseMnemonic()).thenReturn("CS");
        when(course9.courseNumber()).thenReturn(1501);
        when(user1.getUsername()).thenReturn("user1");
        when(user1.getPassword()).thenReturn("password1");
        when(user2.getUsername()).thenReturn("user2");
        when(user2.getPassword()).thenReturn("password2");
        when(user3.getUsername()).thenReturn("user3");
        when(user3.getPassword()).thenReturn("password3");
        when(user4.getUsername()).thenReturn("user4");
        when(user4.getPassword()).thenReturn("password4");
        when(user5.getUsername()).thenReturn("user5");
        when(user5.getPassword()).thenReturn("password5");
        when(user6.getUsername()).thenReturn("user6");
        when(user6.getPassword()).thenReturn("password6");
        when(user7.getUsername()).thenReturn("user7");
        when(user7.getPassword()).thenReturn("password7");
        when(user8.getUsername()).thenReturn("user8");
        when(user8.getPassword()).thenReturn("password8");
        when(review1.getRating()).thenReturn(5);
        when(review1.getReviewContent()).thenReturn("Great course!");
        when(review1.getCourse()).thenReturn(course1);
        when(review1.getUser()).thenReturn(user1);
        when(review2.getRating()).thenReturn(1);
        when(review2.getReviewContent()).thenReturn("Bad course!");
        when(review2.getCourse()).thenReturn(course2);
        when(review2.getUser()).thenReturn(user2);
        when(review3.getRating()).thenReturn(3);
        when(review3.getReviewContent()).thenReturn("Average course!");
        when(review3.getCourse()).thenReturn(course3);
        when(review3.getUser()).thenReturn(user3);
        when(review4.getRating()).thenReturn(4);
        when(review4.getReviewContent()).thenReturn("Good course!");
        when(review4.getCourse()).thenReturn(course4);
        when(review4.getUser()).thenReturn(user4);
        when(review5.getRating()).thenReturn(2);
        when(review5.getReviewContent()).thenReturn("Poor course!");
        when(review5.getCourse()).thenReturn(course5);
        when(review5.getUser()).thenReturn(user5);
        when(review6.getRating()).thenReturn(5);
        when(review6.getReviewContent()).thenReturn("Great course!");
        when(review6.getCourse()).thenReturn(course6);
        when(review6.getUser()).thenReturn(user6);
        when(review7.getRating()).thenReturn(1);
        when(review7.getReviewContent()).thenReturn("Bad course!");
        when(review7.getCourse()).thenReturn(course7);
        when(review7.getUser()).thenReturn(user1);
        when(review8.getRating()).thenReturn(3);
        when(review8.getReviewContent()).thenReturn("Average course!");
        when(review8.getCourse()).thenReturn(course8);
        when(review8.getUser()).thenReturn(user1);
        when(review9.getRating()).thenReturn(4);
        when(review9.getReviewContent()).thenReturn("Good course!");
        when(review9.getCourse()).thenReturn(course1);
        when(review9.getUser()).thenReturn(user8);
        databaseService.saveCourse(course1);
        databaseService.saveCourse(course2);
        databaseService.saveCourse(course3);
        databaseService.saveCourse(course4);
        databaseService.saveCourse(course5);
        databaseService.saveCourse(course6);
        databaseService.saveCourse(course7);
        databaseService.saveCourse(course8);
        databaseService.saveUser(user1);
        databaseService.saveUser(user2);
        databaseService.saveUser(user3);
        databaseService.saveUser(user4);
        databaseService.saveUser(user5);
        databaseService.saveUser(user6);
        databaseService.saveUser(user7);
        databaseService.saveUser(user8);
        databaseService.saveReview(review1);
        databaseService.saveReview(review2);
        databaseService.saveReview(review3);
        databaseService.saveReview(review4);
        databaseService.saveReview(review5);
        databaseService.saveReview(review6);
        databaseService.saveReview(review7);
        databaseService.saveReview(review8);
        databaseService.saveReview(review9);
    }

    @Test
    void getReviewsByUserOneResult() {
        reviewsSetup();
        User user1 = databaseService.getUserByUsername("user2").get();
        Optional<Set<Review>> reviewsOptional = databaseService.getReviewsByUser(user1);
        assertTrue(reviewsOptional.isPresent());
        Set<Review> reviews = reviewsOptional.get();
        assertEquals(1, reviews.size());
        Review review = reviews.iterator().next();
        assertEquals(1, review.getRating());
        assertEquals("Bad course!", review.getReviewContent());
        assertEquals("CS", review.getCourse().courseMnemonic());
        assertEquals(2150, review.getCourse().courseNumber());
        assertEquals("Program and Data Representation", review.getCourse().courseTitle());
        assertEquals("user2", review.getUser().getUsername());
        assertEquals("password2", review.getUser().getPassword());
    }

    @Test
    void getReviewsByUserThreeResults() {
        reviewsSetup();
        User user1 = databaseService.getUserByUsername("user1").get();
        Optional<Set<Review>> reviewsOptional = databaseService.getReviewsByUser(user1);
        assertTrue(reviewsOptional.isPresent());
        Set<Review> reviews = reviewsOptional.get();
        assertEquals(3, reviews.size());
        for (Review review : reviews) {
            assertTrue(review.getUser().getUsername().equals("user1")
                    && review.getUser().getPassword().equals("password1")
                    && review.getCourse().courseMnemonic().equals("CS")
                    && review.getCourse().courseNumber() == 3140
                    && review.getCourse().courseTitle().equals("Software Development Essentials")
                    && review.getRating() == 5 && review.getReviewContent().equals("Great course!")
                    || review.getUser().getUsername().equals("user1")
                    && review.getUser().getPassword().equals("password1")
                    && review.getCourse().courseMnemonic().equals("RELJ")
                    && review.getCourse().courseNumber() == 1210
                    && review.getCourse().courseTitle().equals("Hebrew Bible/Old Testament")
                    && review.getRating() == 1 && review.getReviewContent().equals("Bad course!")
                    || review.getUser().getUsername().equals("user1")
                    && review.getUser().getPassword().equals("password1")
                    && review.getCourse().courseMnemonic().equals("CS")
                    && review.getCourse().courseNumber() == 2100
                    && review.getCourse().courseTitle().equals("Data Structures and Algorithms")
                    && review.getRating() == 3 && review.getReviewContent().equals("Average course!"));
        }
    }

    @Test
    void getReviewsByUserZeroResults() {
        reviewsSetup();
        User user1 = databaseService.getUserByUsername("user7").get();
        Optional<Set<Review>> reviewsOptional = databaseService.getReviewsByUser(user1);
        assertTrue(reviewsOptional.isEmpty());
    }

    @Test
    void getReviewByUserAndCourse() {
        reviewsSetup();
        User user1 = databaseService.getUserByUsername("user1").get();
        Course course1 = databaseService.getCourse("CS", 2100, "Data Structures and Algorithms").get();
        Optional<Review> reviewOptional = databaseService.getReviewByUserAndCourse(user1, course1);
        assertTrue(reviewOptional.isPresent());
        Review review = reviewOptional.get();
        assertEquals(3, review.getRating());
        assertEquals("Average course!", review.getReviewContent());
        assertEquals("CS", review.getCourse().courseMnemonic());
        assertEquals(2100, review.getCourse().courseNumber());
        assertEquals("Data Structures and Algorithms", review.getCourse().courseTitle());
        assertEquals("user1", review.getUser().getUsername());
        assertEquals("password1", review.getUser().getPassword());
    }

    @Test
    void getReviewByUserAndCourseNotPresent() {
        reviewsSetup();
        User user1 = databaseService.getUserByUsername("user1").get();
        Course course1 = databaseService.getCourse("BIOL", 3030, "Biochemistry").get();
        Optional<Review> reviewOptional = databaseService.getReviewByUserAndCourse(user1, course1);
        assertTrue(reviewOptional.isEmpty());
    }

    @Test
    void getReviewsByCourseOneResult() {
        reviewsSetup();
        Course course1 = databaseService.getCourse("CS", 2150, "Program and Data Representation").get();
        Optional<Set<Review>> reviewsOptional = databaseService.getReviewsByCourse(course1);
        assertTrue(reviewsOptional.isPresent());
        Set<Review> reviews = reviewsOptional.get();
        assertEquals(1, reviews.size());
        Review review = reviews.iterator().next();
        assertEquals(1, review.getRating());
        assertEquals("Bad course!", review.getReviewContent());
        assertEquals("CS", review.getCourse().courseMnemonic());
        assertEquals(2150, review.getCourse().courseNumber());
        assertEquals("Program and Data Representation", review.getCourse().courseTitle());
        assertEquals("user2", review.getUser().getUsername());
        assertEquals("password2", review.getUser().getPassword());
    }

    @Test
    void getReviewsByCourseTwoResults() {
        reviewsSetup();
        Course course1 = databaseService.getCourse("CS", 3140, "Software Development Essentials").get();
        Optional<Set<Review>> reviewsOptional = databaseService.getReviewsByCourse(course1);
        assertTrue(reviewsOptional.isPresent());
        Set<Review> reviews = reviewsOptional.get();
        assertEquals(2, reviews.size());
        for (Review review : reviews) {
            assertTrue(review.getUser().getUsername().equals("user1")
                    && review.getUser().getPassword().equals("password1")
                    && review.getCourse().courseMnemonic().equals("CS")
                    && review.getCourse().courseNumber() == 3140
                    && review.getCourse().courseTitle().equals("Software Development Essentials")
                    && review.getRating() == 5 && review.getReviewContent().equals("Great course!")
                    || review.getUser().getUsername().equals("user8")
                    && review.getUser().getPassword().equals("password8")
                    && review.getCourse().courseMnemonic().equals("CS")
                    && review.getCourse().courseNumber() == 3140
                    && review.getCourse().courseTitle().equals("Software Development Essentials")
                    && review.getRating() == 4 && review.getReviewContent().equals("Good course!"));
        }
    }

    @Test
    void getReviewsByCourseZeroResults() {
        reviewsSetup();
        Course course1 = new Course("MATH", 1200, "Some stupid math class");
        Optional<Set<Review>> reviewsOptional = databaseService.getReviewsByCourse(course1);
        assertTrue(reviewsOptional.isEmpty());
    }

    @Test
    void saveUser() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("user");
        when(user.getPassword()).thenReturn("password");
        databaseService.saveUser(user);
        Set<User> users = databaseService.getUsers();
        assertEquals(1, users.size());
        User savedUser = users.iterator().next();
        assertEquals("user", savedUser.getUsername());
        assertEquals("password", savedUser.getPassword());
    }

    @Test
    void saveCourse() {
        Course course = mock(Course.class);
        when(course.courseMnemonic()).thenReturn("CS");
        when(course.courseNumber()).thenReturn(3140);
        when(course.courseTitle()).thenReturn("Software Development Essentials");
        databaseService.saveCourse(course);
        Set<Course> courses = databaseService.getCourses();
        assertEquals(1, courses.size());
        Course savedCourse = courses.iterator().next();
        assertEquals("CS", savedCourse.courseMnemonic());
        assertEquals(3140, savedCourse.courseNumber());
        assertEquals("Software Development Essentials", savedCourse.courseTitle());
    }

    @Test
    void saveReview() {
        Review review = mock(Review.class);
        Course course = mock(Course.class);
        User user = mock(User.class);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        when(course.courseMnemonic()).thenReturn("CS");
        when(course.courseNumber()).thenReturn(3140);
        when(course.courseTitle()).thenReturn("Software Development Essentials");
        when(user.getUsername()).thenReturn("user");
        when(user.getPassword()).thenReturn("password");
        when(review.getRating()).thenReturn(5);
        when(review.getReviewContent()).thenReturn("Great course!");
        when(review.getCourse()).thenReturn(course);
        when(review.getUser()).thenReturn(user);
        when(review.getTimestamp()).thenReturn(timestamp);
        databaseService.saveCourse(course);
        databaseService.saveUser(user);
        databaseService.saveReview(review);
        Set<Review> reviews = databaseService.getReviews();
        assertEquals(1, reviews.size());
        Review savedReview = reviews.iterator().next();
        assertEquals(5, savedReview.getRating());
        assertEquals("Great course!", savedReview.getReviewContent());
        assertEquals("CS", savedReview.getCourse().courseMnemonic());
        assertEquals(3140, savedReview.getCourse().courseNumber());
        assertEquals("Software Development Essentials", savedReview.getCourse().courseTitle());
        assertEquals("user", savedReview.getUser().getUsername());
        assertEquals("password", savedReview.getUser().getPassword());
    }

    void deleteReviewSetup() {
        Review review = mock(Review.class);
        Course course = mock(Course.class);
        User user = mock(User.class);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        when(course.courseMnemonic()).thenReturn("CS");
        when(course.courseNumber()).thenReturn(3140);
        when(course.courseTitle()).thenReturn("Software Development Essentials");
        when(user.getUsername()).thenReturn("user");
        when(user.getPassword()).thenReturn("password");
        when(review.getRating()).thenReturn(5);
        when(review.getReviewContent()).thenReturn("Great course!");
        when(review.getCourse()).thenReturn(course);
        when(review.getUser()).thenReturn(user);
        when(review.getTimestamp()).thenReturn(timestamp);
        databaseService.saveCourse(course);
        databaseService.saveUser(user);
        databaseService.saveReview(review);
    }

    @Test
    void deleteReview_ReviewObject() {
        deleteReviewSetup();
        Set<Review> reviews = databaseService.getReviews();
        assertEquals(1, reviews.size());
        Review savedReview = reviews.iterator().next();
        assertEquals(5, savedReview.getRating());
        assertEquals("Great course!", savedReview.getReviewContent());
        assertEquals("CS", savedReview.getCourse().courseMnemonic());
        assertEquals(3140, savedReview.getCourse().courseNumber());
        assertEquals("Software Development Essentials", savedReview.getCourse().courseTitle());
        assertEquals("user", savedReview.getUser().getUsername());
        assertEquals("password", savedReview.getUser().getPassword());
        databaseService.deleteReview(savedReview);
        reviews = databaseService.getReviews();
        assertEquals(0, reviews.size());
    }

    @Test
    void deleteReview_ReviewTraits() {
        deleteReviewSetup();
        Set<Review> reviews = databaseService.getReviews();
        assertEquals(1, reviews.size());
        Review savedReview = reviews.iterator().next();
        assertEquals(5, savedReview.getRating());
        assertEquals("Great course!", savedReview.getReviewContent());
        assertEquals("CS", savedReview.getCourse().courseMnemonic());
        assertEquals(3140, savedReview.getCourse().courseNumber());
        assertEquals("Software Development Essentials", savedReview.getCourse().courseTitle());
        assertEquals("user", savedReview.getUser().getUsername());
        assertEquals("password", savedReview.getUser().getPassword());
        databaseService.deleteReview("user", "CS", 3140, "Software Development Essentials");
        reviews = databaseService.getReviews();
        assertEquals(0, reviews.size());

    }
}