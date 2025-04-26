package edu.virginia.sde.reviews;

import edu.virginia.sde.reviews.backend.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CourseReviewsApplication extends Application {
//    public static void main(String[] args) {
//        launch(args);
//    }
//
////    @Override
////    public void start(Stage stage) throws Exception {
////        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-world.fxml"));
////        Scene scene = new Scene(fxmlLoader.load());
////        stage.setTitle("Hello World");
////        stage.setScene(scene);
////        stage.show();
////    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Log-inScreen.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Course Reviews");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DatabaseService.getInstance().closeConnection();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
