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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class InterviewScheduleController implements StudentChildController {

    @FXML private TableView<JsonNode> roundTable;
    @FXML private TableColumn<JsonNode, String> colCompany;
    @FXML private TableColumn<JsonNode, String> colRound;
    @FXML private TableColumn<JsonNode, String> colTime;
    @FXML private TableColumn<JsonNode, String> colVenue;
    @FXML private TableColumn<JsonNode, String> colInstructions;
    @FXML private Label statusLabel;

    private StudentDashboardController parentController;

    @Override
    public void setParentController(StudentDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        // Company column: shows "Job #<id>" until we have company name in response
        colCompany.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("companyName").asText()));

        colRound.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("roundName").asText()));

        // Replace the ugly "T" separator in ISO datetime with a space
        colTime.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().path("scheduledTime").asText().replace("T", " ")));

        colVenue.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("venueOrLink").asText()));

        colInstructions.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("instructions").asText()));

        loadMyInterviews();
    }

    private void loadMyInterviews() {
        statusLabel.setText("Loading your interview schedule...");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/rounds/my-interviews"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                System.out.println("[InterviewSchedule] Status: " + response.statusCode());
                System.out.println("[InterviewSchedule] Body: " + response.body());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());

                    ObservableList<JsonNode> rounds = FXCollections.observableArrayList();
                    for (JsonNode r : array) {
                        rounds.add(r);
                    }

                    Platform.runLater(() -> {
                        roundTable.setItems(rounds);
                        if (rounds.isEmpty()) {
                            statusLabel.setText(
                                    "No interviews scheduled yet. Check back after your application is reviewed.");
                        } else {
                            statusLabel.setText(
                                    "You have " + rounds.size() + " interview round(s) scheduled.");
                        }
                    });

                } else {
                    Platform.runLater(() ->
                            statusLabel.setText("Could not load schedule (Error " 
                                    + response.statusCode() + "). Is the backend running?"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        statusLabel.setText("Error connecting to server. Is the backend running on port 8080?"));
            }
        }).start();
    }
}
