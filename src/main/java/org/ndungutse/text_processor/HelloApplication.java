package org.ndungutse.text_processor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try{
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 820, 440);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        }catch (IOException e){
            System.out.println(e.fillInStackTrace());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}