package com.bank.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private Label titleLabel, hintLabel, errorLabel;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox hintCheck;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.web("#1b5e20"));

        String fieldStyle = "-fx-background-color: #f1f8e9; -fx-border-color: #81c784; -fx-border-radius: 4;";

        loginField.setPromptText("Логин");
        loginField.setPrefWidth(220);
        loginField.setFont(Font.font("Arial", 14));
        loginField.setPadding(new Insets(6, 8, 6, 8));
        loginField.setStyle(fieldStyle);
        loginField.setOnAction(e -> passwordField.requestFocus());

        passwordField.setPromptText("Пароль");
        passwordField.setPrefWidth(220);
        passwordField.setFont(Font.font("Arial", 14));
        passwordField.setPadding(new Insets(6, 8, 6, 8));
        passwordField.setStyle(fieldStyle);
        passwordField.setOnAction(e -> onLogin());

        hintCheck.setFont(Font.font("Arial", 13));
        hintCheck.setTextFill(Color.DARKSLATEGRAY);
        hintCheck.setPadding(new Insets(4));
        hintCheck.setOnAction(e -> hintLabel.setVisible(hintCheck.isSelected()));

        hintLabel.setText("Демо: ivanov / 1234");
        hintLabel.setFont(Font.font("Arial", 13));
        hintLabel.setTextFill(Color.GRAY);
        hintLabel.setVisible(false);
        hintLabel.managedProperty().bind(hintLabel.visibleProperty());

        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        errorLabel.setTextFill(Color.CRIMSON);

        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        loginButton.setTextFill(Color.WHITE);
        loginButton.setPadding(new Insets(10, 40, 10, 40));
        loginButton.setStyle("-fx-background-color: #2e7d32; -fx-background-radius: 6;");
        loginButton.setCursor(Cursor.HAND);
        loginButton.setOnMouseEntered(e -> loginButton.setOpacity(0.85));
        loginButton.setOnMouseExited(e -> loginButton.setOpacity(1.0));
        loginButton.setOnAction(e -> onLogin());
    }

    private void onLogin() {
        Client client = BankService.authenticate(loginField.getText(), passwordField.getText());
        if (client == null) {
            errorLabel.setText("Неверный логин или пароль");
            passwordField.clear();
            return;
        }
        BankService.startSession(client);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1150, 760));
            stage.centerOnScreen();
        } catch (IOException ex) {
            errorLabel.setText("Ошибка загрузки экрана: " + ex.getMessage());
        }
    }
}