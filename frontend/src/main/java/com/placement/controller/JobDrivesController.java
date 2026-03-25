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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class JobDrivesController implements StudentChildController {

    @FXML private TableView<JsonNode> jobTable;
    @FXML private TableColumn<JsonNode, String> colCompany;
    @FXML private TableColumn<JsonNode, String> colRole;
    @FXML private TableColumn<JsonNode, String> colPackage;
    @FXML private TableColumn<JsonNode, String> colDeadline;
    @FXML private TableColumn<JsonNode, String> colMinCgpa;
    @FXML private TableColumn<JsonNode, String> colBranches;
    @FXML private TableColumn<JsonNode, String> colStatus;
    @FXML private Label statusLabel;

    private StudentDashboardController parentController;

    @Override
    public void setParentController(StudentDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        colCompany.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("companyName").asText()));
        colRole.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("roleDescription").asText()));
        colPackage.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("packageLPA").asText() + " LPA"));
        colDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("deadline").asText()));
        colMinCgpa.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("minCgpa").asText()));
        colBranches.setCellValueFactory(d -> {
            JsonNode branches = d.getValue().path("allowedBranches");
            List<String> list = new ArrayList<>();
            branches.forEach(b -> list.add(b.asText()));
            return new SimpleStringProperty(String.join(", ", list));
        });

        // Status column with color
        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("status").asText()));
        colStatus.setCellFactory(col -> new TableCell<>() {
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
                    array.forEach(jobs::add);
                    Platform.runLater(() -> jobTable.setItems(jobs));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleApply() {
        JsonNode selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("← Please select a job first", false);
            return;
        }

        // Check job is OPEN before allowing apply
        String jobStatus = selected.path("status").asText();
        if (!"OPEN".equals(jobStatus)) {
            setStatus("❌ Applications are closed for "
                + selected.path("companyName").asText()
                + " (Status: " + jobStatus + ")", false);
            return;
        }

        Long jobId = selected.path("id").asLong();
        String company = selected.path("companyName").asText();
        String minCgpa = selected.path("minCgpa").asText();
        String branches = "";
        List<String> branchList = new ArrayList<>();
        selected.path("allowedBranches").forEach(b -> branchList.add(b.asText()));
        if (!branchList.isEmpty()) branches = "\nAllowed Branches: " + String.join(", ", branchList);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Apply for Job");
        confirm.setHeaderText("Apply to " + company + "?");
        confirm.setContentText("Min CGPA required: " + minCgpa
            + branches
            + "\n\nMake sure you meet the criteria before applying.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                setStatus("Submitting application...", true);

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
                                setStatus("✅ Successfully applied to " + company + "!", true);
                            } else if (response.body().contains("already")) {
                                setStatus("⚠️ You have already applied to " + company, false);
                            } else if (response.body().contains("not eligible")
                                    || response.body().contains("eligible")) {
                                setStatus("❌ You do not meet the eligibility criteria for "
                                    + company + ". Check CGPA, percentages and branch.", false);
                            } else if (response.body().contains("closed")) {
                                setStatus("❌ Applications are now closed for " + company, false);
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

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
            ? "-fx-text-fill:#38a169; -fx-font-size:12px;"
            : "-fx-text-fill:#e94560; -fx-font-size:12px;");
    }
}