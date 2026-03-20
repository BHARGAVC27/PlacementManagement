package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SignupController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usnField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> branchCombo;
    @FXML private TextField backlogsField;
    @FXML private TextField cgpaField;
    @FXML private TextField tenthField;
    @FXML private TextField twelfthField;
    @FXML private TextField resumeField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        branchCombo.getItems().addAll(
            "CSE", "ISE", "ECE", "EEE", "MECH", "CIVIL", "AIML", "AIDS", "CSD"
        );
        branchCombo.setValue("CSE");
    }

    @FXML
    private void handleSignup() {
        // Validation
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty()
                || usnField.getText().isEmpty() || phoneField.getText().isEmpty()
                || emailField.getText().isEmpty() || passwordField.getText().isEmpty()
                || cgpaField.getText().isEmpty() || tenthField.getText().isEmpty()
                || twelfthField.getText().isEmpty()) {
            setError("Please fill all required fields.");
            return;
        }

        if (passwordField.getText().length() < 6) {
            setError("Password must be at least 6 characters.");
            return;
        }

        setError("Creating account...");

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("email", emailField.getText().trim());
            body.put("password", passwordField.getText());
            body.put("firstName", firstNameField.getText().trim());
            body.put("lastName", lastNameField.getText().trim());
            body.put("usn", usnField.getText().trim().toUpperCase());
            body.put("phone", phoneField.getText().trim());
            body.put("branch", branchCombo.getValue());
            body.put("currentCgpa", new java.math.BigDecimal(cgpaField.getText().trim()));
            body.put("tenthPercent", new java.math.BigDecimal(tenthField.getText().trim()));
            body.put("twelfthPercent", new java.math.BigDecimal(twelfthField.getText().trim()));
            body.put("activeBacklogs", Integer.parseInt(
                backlogsField.getText().isEmpty() ? "0" : backlogsField.getText().trim()));
            body.put("resumeUrl", resumeField.getText().trim());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(body);

            new Thread(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/register/student"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                    HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                JsonNode json2 = mapper.readTree(response.body());
                                String token = json2.get("token").asText();
                                String role = json2.get("role").asText();
                                String email = json2.get("email").asText();
                                SessionManager.getInstance().setSession(token, role, email);

                                FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/com/placement/fxml/student-dashboard.fxml")
                                );
                                Stage stage = (Stage) emailField.getScene().getWindow();
                                stage.setScene(new Scene(loader.load(), 1000, 700));
                            } catch (Exception e) {
                                setError("Account created but login failed. Please sign in.");
                            }
                        } else if (response.body().contains("already")) {
                            setError("Email already registered. Please sign in.");
                        } else {
                            setError("Error: " + response.body());
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> setError("Cannot connect to server."));
                }
            }).start();

        } catch (NumberFormatException e) {
            setError("Please enter valid numbers for CGPA and percentage fields.");
        } catch (Exception e) {
            setError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/placement/fxml/login.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle(message.contains("Creating")
            ? "-fx-text-fill:#3182ce; -fx-font-size:12px;"
            : "-fx-text-fill:#e94560; -fx-font-size:12px;");
    }
}