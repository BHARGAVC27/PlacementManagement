package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter email and password.");
            return;
        }

        errorLabel.setText("Logging in...");

        new Thread(() -> {
            try {
                String requestBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}", email, password
                );

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(response.body());
                    String token = json.get("token").asText();
                    String role = json.get("role").asText();
                    String userEmail = json.get("email").asText();

                    SessionManager.getInstance().setSession(token, role, userEmail);

                    Platform.runLater(() -> navigateToDashboard(role));
                } else {
                    Platform.runLater(() ->
                        errorLabel.setText("Invalid email or password."));
                }

            } catch (Exception e) {
                Platform.runLater(() ->
                    errorLabel.setText("Cannot connect to server. Is backend running?"));
            }
        }).start();
    }

    private void navigateToDashboard(String role) {
        try {
            String fxml = role.equals("ADMIN") || role.equals("PLACEMENT_OFFICER")
                ? "/com/placement/fxml/admin-dashboard.fxml"
                : "/com/placement/fxml/student-dashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1000, 700));
        } catch (Exception e) {
            errorLabel.setText("Error loading dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void goToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/placement/fxml/signup.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 800, 700));
        } catch (Exception e) {
            errorLabel.setText("Error loading signup page.");
            e.printStackTrace();
        }
    }
}