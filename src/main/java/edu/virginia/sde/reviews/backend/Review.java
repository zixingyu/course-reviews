package edu.virginia.sde.reviews.backend;

import java.sql.Timestamp;
public class Review {

    private Course course;
    private User user;
    private String reviewContent;
    private Timestamp timestamp;
    private int rating;

    public Review(Course course, User user, String reviewContent, Timestamp timestamp, int rating) {
        this.course = course;
        this.user = user;
        this.reviewContent = reviewContent;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getRating() {
        return rating;
    }
    public void setRating(int rating){
        this.rating = rating;
    }

    @Override
    public String toString() {
        return  "Rating: " + rating+ "  "+
                "Time" + " " +
                timestamp +
                "\n" + "\n"+
                "ReviewContent: "+ reviewContent;
    }
}
