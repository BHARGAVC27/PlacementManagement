package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.http.*;

public class AdminHomeController implements AdminChildController {

    @FXML private Label statTotalJobs;
    @FXML private Label statTotalStudents;
    @FXML private Label statPlaced;
    @FXML private Label statOngoing;
    @FXML private TableView<JsonNode> jobTable;
    @FXML private TableColumn<JsonNode, String> colCompany;
    @FXML private TableColumn<JsonNode, String> colRole;
    @FXML private TableColumn<JsonNode, String> colPackage;
    @FXML private TableColumn<JsonNode, String> colDeadline;
    @FXML private TableColumn<JsonNode, String> colStatus;

    private AdminDashboardController parentController;

    @Override
    public void setParentController(AdminDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        statTotalJobs.setText("...");
        statTotalStudents.setText("0");
        statPlaced.setText("0");
        statOngoing.setText("...");

        // Set up table columns
        colCompany.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("companyName").asText()));
        colRole.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("roleDescription").asText()));
        colPackage.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("packageLPA").asText() + " LPA"));
        colDeadline.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("deadline").asText()));
        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("status").asText()));

        loadJobs();
    }

    private void loadJobs() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/jobs"))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());

                    ObservableList<JsonNode> jobs = FXCollections.observableArrayList();
                    long ongoing = 0;
                    for (JsonNode job : array) {
                        jobs.add(job);
                        if ("OPEN".equals(job.path("status").asText()) ||
                            "ONGOING".equals(job.path("status").asText())) {
                            ongoing++;
                        }
                    }

                    final long ongoingCount = ongoing;
                    final int totalJobs = jobs.size();

                    Platform.runLater(() -> {
                        jobTable.setItems(jobs);
                        statTotalJobs.setText(String.valueOf(totalJobs));
                        statOngoing.setText(String.valueOf(ongoingCount));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}