package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StudentProfileController implements StudentChildController {

    // ── Personal info labels (read-only)
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblUsn;
    @FXML private Label lblPhone;

    // ── Academic info labels (shown in view mode)
    @FXML private Label lblBranch;
    @FXML private Label lblCgpa;
    @FXML private Label lblTenth;
    @FXML private Label lblTwelfth;
    @FXML private Label lblBacklogs;
    @FXML private Label lblResume;

    // ── Academic edit fields (shown in edit mode)
    @FXML private VBox editBox;
    @FXML private VBox viewBox;
    @FXML private TextField tfBranch;
    @FXML private TextField tfCgpa;
    @FXML private TextField tfTenth;
    @FXML private TextField tfTwelfth;
    @FXML private TextField tfBacklogs;
    @FXML private TextField tfResume;

    @FXML private Button btnEdit;
    @FXML private Label statusLabel;

    private StudentDashboardController parentController;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setParentController(StudentDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        editBox.setVisible(false);
        editBox.setManaged(false);
        loadProfile();
    }

    private void loadProfile() {
        statusLabel.setText("Loading profile...");
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                // Load personal info (name, email, usn, phone) from /api/students/me
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students/me"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .GET().build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                // Load academic profile from /api/students/profile
                HttpRequest req2 = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students/profile"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .GET().build();
                HttpResponse<String> res2 = client.send(req2, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    try {
                        // Fill personal info
                        if (res.statusCode() == 200) {
                            JsonNode me = mapper.readTree(res.body());
                            lblName.setText(me.path("firstName").asText("") + " " + me.path("lastName").asText(""));
                            lblEmail.setText(me.path("email").asText("—"));
                            lblUsn.setText(me.path("usn").asText("—"));
                            lblPhone.setText(me.path("phone").asText("—"));
                        }
                        // Fill academic info
                        if (res2.statusCode() == 200) {
                            JsonNode p = mapper.readTree(res2.body());
                            lblBranch.setText(p.path("branch").asText("—"));
                            lblCgpa.setText(p.path("currentCgpa").asText("—"));
                            lblTenth.setText(p.path("tenthPercent").asText("—") + "%");
                            lblTwelfth.setText(p.path("twelfthPercent").asText("—") + "%");
                            lblBacklogs.setText(p.path("activeBacklogs").asText("0"));
                            lblResume.setText(p.path("resumeUrl").asText("Not provided"));
                            // Pre-fill edit fields too
                            tfBranch.setText(p.path("branch").asText(""));
                            tfCgpa.setText(p.path("currentCgpa").asText(""));
                            tfTenth.setText(p.path("tenthPercent").asText(""));
                            tfTwelfth.setText(p.path("twelfthPercent").asText(""));
                            tfBacklogs.setText(p.path("activeBacklogs").asText("0"));
                            tfResume.setText(p.path("resumeUrl").asText(""));
                        }
                        statusLabel.setText("");
                    } catch (Exception e) {
                        statusLabel.setText("Error parsing profile data.");
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error connecting to server."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleEditSave() {
        if (!editBox.isVisible()) {
            // Switch to edit mode
            viewBox.setVisible(false);
            viewBox.setManaged(false);
            editBox.setVisible(true);
            editBox.setManaged(true);
            btnEdit.setText("Save Changes");
            statusLabel.setText("Edit your academic details below.");
        } else {
            // Save mode — call PUT /api/students/profile
            saveProfile();
        }
    }

    private void saveProfile() {
        statusLabel.setText("Saving...");
        new Thread(() -> {
            try {
                ObjectNode body = mapper.createObjectNode();
                body.put("branch", tfBranch.getText().trim());
                body.put("currentCgpa", Double.parseDouble(tfCgpa.getText().trim()));
                body.put("tenthPercent", Double.parseDouble(tfTenth.getText().trim()));
                body.put("twelfthPercent", Double.parseDouble(tfTwelfth.getText().trim()));
                body.put("activeBacklogs", Integer.parseInt(tfBacklogs.getText().trim()));
                body.put("resumeUrl", tfResume.getText().trim());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students/profile"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        // Switch back to view mode and reload
                        editBox.setVisible(false);
                        editBox.setManaged(false);
                        viewBox.setVisible(true);
                        viewBox.setManaged(true);
                        btnEdit.setText("Edit Profile");
                        statusLabel.setText("Profile updated successfully!");
                        loadProfile();
                    } else {
                        statusLabel.setText("Save failed. Check your inputs and try again.");
                    }
                });
            } catch (NumberFormatException e) {
                Platform.runLater(() -> statusLabel.setText("Invalid input — CGPA, 10th, 12th must be numbers."));
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error saving profile."));
                e.printStackTrace();
            }
        }).start();
    }
}
