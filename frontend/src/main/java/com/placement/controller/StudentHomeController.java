package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.http.*;

public class StudentHomeController implements StudentChildController {

    @FXML private Label statOpenJobs;
    @FXML private Label statApplied;
    @FXML private Label statShortlisted;
    @FXML private Label statInterviews;
    @FXML private TableView<JsonNode> jobTable;
    @FXML private TableColumn<JsonNode, String> colCompany;
    @FXML private TableColumn<JsonNode, String> colRole;
    @FXML private TableColumn<JsonNode, String> colPackage;
    @FXML private TableColumn<JsonNode, String> colDeadline;
    @FXML private TableColumn<JsonNode, String> colStatus;
    @FXML private Label applyStatus;

    private StudentDashboardController parentController;

    @Override
    public void setParentController(StudentDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        statOpenJobs.setText("...");
        statApplied.setText("...");
        statShortlisted.setText("0");
        statInterviews.setText("0");

        colCompany.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("companyName").asText()));
        colRole.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("roleDescription").asText()));
        colPackage.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("packageLPA").asText() + " LPA"));
        colDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("deadline").asText()));

        // Status column with color
        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("status").asText()));
        colStatus.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                setStyle(switch (status) {
                    case "OPEN"        -> "-fx-text-fill:#38a169; -fx-font-weight:bold;";
                    case "ONGOING"     -> "-fx-text-fill:#d69e2e; -fx-font-weight:bold;";
                    case "RESULTS_OUT" -> "-fx-text-fill:#805ad5; -fx-font-weight:bold;";
                    case "CLOSED"      -> "-fx-text-fill:#718096; -fx-font-weight:bold;";
                    default            -> "";
                });
            }
        });

        loadJobs();
        loadMyApplicationStats();
    }

    private void loadJobs() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/jobs"))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());
                    ObservableList<JsonNode> jobs = FXCollections.observableArrayList();
                    long openCount = 0;
                    for (JsonNode job : array) {
                        jobs.add(job);
                        if ("OPEN".equals(job.path("status").asText())) openCount++;
                    }
                    final long open = openCount;
                    Platform.runLater(() -> {
                        jobTable.setItems(jobs);
                        statOpenJobs.setText(String.valueOf(open));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadMyApplicationStats() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/applications/my"))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());
                    long applied = 0, shortlisted = 0, interviews = 0;
                    for (JsonNode app : array) {
                        applied++;
                        String status = app.path("currentStatus").asText();
                        if (status.contains("SHORTLISTED") || status.equals("OA_CLEARED"))
                            shortlisted++;
                        if (status.equals("INTERVIEW_SCHEDULED"))
                            interviews++;
                    }
                    final long a = applied, s = shortlisted, i = interviews;
                    Platform.runLater(() -> {
                        statApplied.setText(String.valueOf(a));
                        statShortlisted.setText(String.valueOf(s));
                        statInterviews.setText(String.valueOf(i));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleQuickApply() {
        JsonNode selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("← Please select a job first", false);
            return;
        }

        // Check job is still OPEN
        String jobStatus = selected.path("status").asText();
        if (!"OPEN".equals(jobStatus)) {
            setStatus("❌ Applications are closed for "
                + selected.path("companyName").asText()
                + " (Status: " + jobStatus + ")", false);
            return;
        }

        Long jobId = selected.path("id").asLong();
        String company = selected.path("companyName").asText();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Apply for Job");
        confirm.setHeaderText("Apply to " + company + "?");
        confirm.setContentText("This will submit your application.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                setStatus("Submitting...", true);
                new Thread(() -> {
                    try {
                        String body = "{\"jobPostId\":" + jobId + "}";
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/applications"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();

                        HttpResponse<String> response = client.send(
                            request, HttpResponse.BodyHandlers.ofString());

                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                setStatus("✅ Applied to " + company + "!", true);
                                loadMyApplicationStats();
                            } else if (response.body().contains("already")) {
                                setStatus("⚠️ Already applied to " + company, false);
                            } else if (response.body().contains("not eligible")) {
                                setStatus("❌ You do not meet the eligibility criteria for "
                                    + company, false);
                            } else if (response.body().contains("closed")) {
                                setStatus("❌ Applications are closed for " + company, false);
                            } else {
                                setStatus("❌ Error: " + response.body(), false);
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() ->
                            setStatus("❌ Cannot connect to server.", false));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void goToJobDrives() {
        if (parentController != null)
            parentController.showJobDrives();
    }

    private void setStatus(String message, boolean success) {
        applyStatus.setText(message);
        applyStatus.setStyle(success
            ? "-fx-text-fill:#38a169; -fx-font-size:12px;"
            : "-fx-text-fill:#e94560; -fx-font-size:12px;");
    }
}