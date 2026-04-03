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
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StudentDatabaseController {

    @FXML private TableView<JsonNode> studentTable;
    @FXML private TableColumn<JsonNode, String> colName;
    @FXML private TableColumn<JsonNode, String> colUSN;
    @FXML private TableColumn<JsonNode, String> colEmail;
    @FXML private TableColumn<JsonNode, String> colBranch;
    @FXML private TableColumn<JsonNode, String> colCGPA;
    @FXML private TableColumn<JsonNode, String> col10th;
    @FXML private TableColumn<JsonNode, String> col12th;
    @FXML private TableColumn<JsonNode, String> colBacklogs;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;

    private ObservableList<JsonNode> allStudents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(d -> {
            String first = d.getValue().path("firstName").asText("");
            String last = d.getValue().path("lastName").asText("");
            return new SimpleStringProperty(first + " " + last);
        });
        colUSN.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("usn").asText("N/A")));
        colEmail.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("email").asText()));
        colBranch.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("branch").asText("N/A")));
        colCGPA.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("currentCgpa").asText("N/A")));
        col10th.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("tenthPercent").asText("N/A")));
        col12th.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("twelfthPercent").asText("N/A")));
        colBacklogs.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().path("activeBacklogs").asText("N/A")));

        // Live search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterStudents(newVal));

        loadAllStudents();
    }

    private void loadAllStudents() {
        statusLabel.setText("Loading student database...");
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/students/all"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                System.out.println("[StudentDB] Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());

                    for (JsonNode s : array) allStudents.add(s);

                    Platform.runLater(() -> {
                        studentTable.setItems(allStudents);
                        statusLabel.setText("Total students registered: " + allStudents.size());
                    });
                } else {
                    Platform.runLater(() ->
                            statusLabel.setText("Failed to load students (Error " + response.statusCode() + ")"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Error connecting to server."));
            }
        }).start();
    }

    private void filterStudents(String query) {
        if (query == null || query.isBlank()) {
            studentTable.setItems(allStudents);
            statusLabel.setText("Total students registered: " + allStudents.size());
            return;
        }
        String lower = query.toLowerCase();
        ObservableList<JsonNode> filtered = FXCollections.observableArrayList();
        for (JsonNode s : allStudents) {
            String name = (s.path("firstName").asText("") + " " + s.path("lastName").asText("")).toLowerCase();
            String usn = s.path("usn").asText("").toLowerCase();
            String branch = s.path("branch").asText("").toLowerCase();
            String email = s.path("email").asText("").toLowerCase();
            if (name.contains(lower) || usn.contains(lower)
                    || branch.contains(lower) || email.contains(lower)) {
                filtered.add(s);
            }
        }
        studentTable.setItems(filtered);
        statusLabel.setText("Showing " + filtered.size() + " result(s) for \"" + query + "\"");
    }
}
