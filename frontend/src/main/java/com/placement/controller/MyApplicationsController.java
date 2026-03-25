package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyApplicationsController implements StudentChildController {

    @FXML private Label totalCount;
    @FXML private Label shortlistedCount;
    @FXML private Label interviewCount;
    @FXML private Label offeredCount;
    @FXML private Label rejectedCount;
    @FXML private TableView<JsonNode> appTable;
    @FXML private TableColumn<JsonNode, String> colCompany;
    @FXML private TableColumn<JsonNode, String> colAppliedDate;
    @FXML private TableColumn<JsonNode, String> colStatus;
    @FXML private TableColumn<JsonNode, String> colRemarks;
    @FXML private Label statusLabel;

    private StudentDashboardController parentController;
    private final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Override
    public void setParentController(StudentDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        totalCount.setText("...");
        shortlistedCount.setText("...");
        interviewCount.setText("...");
        offeredCount.setText("...");
        rejectedCount.setText("...");

        colCompany.setCellValueFactory(d ->
            new SimpleStringProperty(
                d.getValue().path("companyName").asText()));

        colAppliedDate.setCellValueFactory(d -> {
            String raw = d.getValue().path("appliedDate").asText();
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return new SimpleStringProperty(dt.format(formatter));
            } catch (Exception e) {
                return new SimpleStringProperty(raw);
            }
        });

        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(
                d.getValue().path("currentStatus").asText()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setStyle(""); return;
                }
                setText(formatStatus(status));
                setStyle(getBadgeStyle(status));
            }
        });

        colRemarks.setCellValueFactory(d -> {
            String remarks = d.getValue().path("remarks").asText();
            return new SimpleStringProperty(
                remarks.isEmpty() || remarks.startsWith("REJECTED_FROM")
                    ? "—" : remarks);
        });

        loadApplications();
    }

    private void loadApplications() {
        statusLabel.setText("Loading...");
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                        "http://localhost:8080/api/applications/my"))
                    .header("Authorization",
                        "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());

                    ObservableList<JsonNode> apps =
                        FXCollections.observableArrayList();

                    long total = 0, shortlisted = 0,
                         interviews = 0, offered = 0, rejected = 0;

                    for (JsonNode app : array) {
                        apps.add(app);
                        total++;
                        String st = app.path("currentStatus").asText();

                        // Shortlisted = reached OA or beyond
                        if (st.equals("SHORTLISTED_OA")
                                || st.equals("OA_CLEARED")
                                || st.equals("INTERVIEW_SCHEDULED")
                                || st.equals("OFFERED")) {
                            shortlisted++;
                        }

                        // Interview = reached interview or beyond
                        if (st.equals("INTERVIEW_SCHEDULED")
                                || st.equals("OFFERED")) {
                            interviews++;
                        }

                        if (st.equals("OFFERED")) offered++;
                        if (st.equals("REJECTED")) rejected++;
                    }

                    final long t = total, s = shortlisted,
                               i = interviews, o = offered, r = rejected;

                    Platform.runLater(() -> {
                        appTable.setItems(apps);
                        totalCount.setText(String.valueOf(t));
                        shortlistedCount.setText(String.valueOf(s));
                        interviewCount.setText(String.valueOf(i));
                        offeredCount.setText(String.valueOf(o));
                        rejectedCount.setText(String.valueOf(r));
                        statusLabel.setText(t == 0
                            ? "You haven't applied to any jobs yet."
                            : t + " application(s) found.");
                    });
                } else {
                    Platform.runLater(() ->
                        statusLabel.setText(
                            "Error loading applications."));
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                    statusLabel.setText("Cannot connect to server."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadApplications();
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "APPLIED"             -> "📝 Applied";
            case "ELIGIBLE"            -> "✅ Eligible";
            case "SHORTLISTED_OA"      -> "⭐ Shortlisted for OA";
            case "OA_CLEARED"          -> "🎯 OA Cleared";
            case "INTERVIEW_SCHEDULED" -> "📅 Interview Scheduled";
            case "OFFERED"             -> "🎉 Offered!";
            case "REJECTED"            -> "❌ Rejected";
            default                    -> status;
        };
    }

    private String getBadgeStyle(String status) {
        String base =
            "-fx-font-weight:bold; -fx-font-size:12px;"
            + "-fx-padding:4 8 4 8;";
        return base + switch (status) {
            case "APPLIED"             ->
                "-fx-text-fill:#3182ce;";
            case "ELIGIBLE"            ->
                "-fx-text-fill:#38a169;";
            case "SHORTLISTED_OA"      ->
                "-fx-text-fill:#d69e2e;";
            case "OA_CLEARED"          ->
                "-fx-text-fill:#805ad5;";
            case "INTERVIEW_SCHEDULED" ->
                "-fx-text-fill:#2b6cb0;";
            case "OFFERED"             ->
                "-fx-text-fill:#276749;"
                + "-fx-background-color:#c6f6d5;"
                + "-fx-background-radius:4;";
            case "REJECTED"            ->
                "-fx-text-fill:#9b2335;"
                + "-fx-background-color:#fed7d7;"
                + "-fx-background-radius:4;";
            default                    ->
                "-fx-text-fill:#4a5568;";
        };
    }
}