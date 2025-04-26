package edu.virginia.sde.reviews.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchTest {

    private Search search;
    @Mock
    private DatabaseService databaseService;  // Mocked DatabaseService

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        search = new Search(databaseService);  // Initialize Search class with the mock
    }

    @Test
    void searchCoursesByTitle() {
        String courseTitle = "Introduction to Programming";
        Set<Course> courseSet = new HashSet<>();
        courseSet.add(new Course("CS", 1110, "Introduction to Programming"));

        // Mock the behavior of databaseService
        when(databaseService.getCoursesByTitle(courseTitle)).thenReturn(Optional.of(courseSet));

        // When
        Optional<Set<Course>> result = search.searchCoursesByTitle(courseTitle);

        // Then
        assertTrue(result.isPresent(), "The result should be present.");
        assertEquals(1, result.get().size(), "The result set should contain 1 course.");
        // Verify that the course title matches
        Course retrievedCourse = result.get().iterator().next();
        assertEquals("CS", retrievedCourse.courseMnemonic(), "The course mnemonic should match.");
        assertEquals(1110, retrievedCourse.courseNumber(), "The course number should match.");
        assertEquals(courseTitle, retrievedCourse.courseTitle(), "The course title should match.");
    }

    @Test
    void searchCoursesByTitle_substring() {
        String courseTitle = "Programming";
        Set<Course> courseSet = new HashSet<>();
        courseSet.add(new Course("CS", 1110, "Introduction to Programming"));

        // Mock the behavior of databaseService
        when(databaseService.getCoursesByTitle(courseTitle)).thenReturn(Optional.of(courseSet));

        // When
        Optional<Set<Course>> result = search.searchCoursesByTitle(courseTitle);

        // Then
        assertTrue(result.isPresent(), "The result should be present.");
        assertEquals(1, result.get().size(), "The result set should contain 1 course.");
        // Verify that the course title matches
        Course retrievedCourse = result.get().iterator().next();
        assertEquals("CS", retrievedCourse.courseMnemonic(), "The course mnemonic should match.");
        assertEquals(1110, retrievedCourse.courseNumber(), "The course number should match.");
    }

    @Test
    void searchCoursesByDepartment() {
        String department = "CS";
        Set<Course> courseSet = new HashSet<>();
        Course course_1 = new Course("CS",2120,"Data Structures and Algorithm I");
        Course course_2 = new Course("CS",2130,"Computer Systems and Organization");
        courseSet.add(course_1);
        courseSet.add(course_2);

        // Mock the behavior of databaseService
        when(databaseService.getCoursesByDepartment(department)).thenReturn(Optional.of(courseSet));

        // When
        Optional<Set<Course>> result = search.searchCoursesByDepartment(department);

        // Then
        assertTrue(result.isPresent(), "The result should be present.");
        assertEquals(2, result.get().size(), "The result set should contain 2 courses.");
        // Verify the course matches
        Set<Course> retrievedCourses = result.get();
        assertTrue(retrievedCourses.contains(course_1), "The result should contain course_1.");
        assertTrue(retrievedCourses.contains(course_2), "The result should contain course_2.");

        // Verify that the databaseService was called with the correct parameter
        verify(databaseService).getCoursesByDepartment(department);
    }

    @Test
    void searchCoursesByNumber() {
        int courseNumber = 3140;
        Set<Course> courseSet = new HashSet<>();
        Course course_1 = new Course("CS",3140,"Software Development Essentials");
        Course course_2 = new Course("PSYC",3140,"RM: New Course in Psychology");
        courseSet.add(course_1);
        courseSet.add(course_2);

        // Mock the behavior of databaseService
        when(databaseService.getCoursesByNumber(3140)).thenReturn(Optional.of(courseSet));

        // When
        Optional<Set<Course>> result = search.searchCoursesByNumber(courseNumber);

        // Then
        assertTrue(result.isPresent(), "The result should be present.");
        assertEquals(2, result.get().size(), "The result set should contain 2 courses.");
        // Verify the course matches
        Set<Course> retrievedCourses = result.get();
        assertTrue(retrievedCourses.contains(course_1), "The result should contain course_1.");
        assertTrue(retrievedCourses.contains(course_2), "The result should contain course_2.");

        // Verify that the databaseService was called with the correct parameter
        verify(databaseService).getCoursesByNumber(courseNumber);
    }
}