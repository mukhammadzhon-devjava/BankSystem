package com.bank.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        BankService.seed();
        Parent root = FXMLLoader.load(Main.class.getResource("login.fxml"));
        stage.setTitle("Банковская система");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> ReportService.onClose(BankService.getCurrentSession()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}