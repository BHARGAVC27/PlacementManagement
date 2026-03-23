package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScheduleRoundsController implements AdminChildController {

    @FXML private ComboBox<JobItem> jobCombo;
    @FXML private ComboBox<String> roundNameCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField venueField;
    @FXML private TextArea instructionsField;
    @FXML private Label formStatus;
    @FXML private Button addBtn;

    @FXML private TableView<JsonNode> roundsTable;
    @FXML private TableColumn<JsonNode, String> colJob;
    @FXML private TableColumn<JsonNode, String> colRound;
    @FXML private TableColumn<JsonNode, String> colDate;
    @FXML private TableColumn<JsonNode, String> colVenue;
    @FXML private TableColumn<JsonNode, String> colInstructions;
    @FXML private Label tableStatus;

    private AdminDashboardController parentController;
    private final ObservableList<JsonNode> rounds = FXCollections.observableArrayList();
    private final DateTimeFormatter displayFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // Map jobId -> companyName for display
    private final Map<Long, String> jobNames = new LinkedHashMap<>();

    @Override
    public void setParentController(AdminDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        // Round name options
        roundNameCombo.getItems().addAll(
            "Online Assessment (OA)",
            "Technical Round 1",
            "Technical Round 2",
            "HR Round",
            "Group Discussion",
            "Case Study",
            "Final Interview"
        );

        // Setup rounds table
        colJob.setCellValueFactory(d -> {
            Long jobId = d.getValue().path("jobPostId").asLong();
            return new SimpleStringProperty(jobNames.getOrDefault(jobId, "Job #" + jobId));
        });
        colRound.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("roundName").asText()));
        colDate.setCellValueFactory(d -> {
            String raw = d.getValue().path("scheduledTime").asText();
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return new SimpleStringProperty(dt.format(displayFormatter));
            } catch (Exception e) {
                return new SimpleStringProperty(raw);
            }
        });
        colVenue.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().path("venueOrLink").asText()));
        colInstructions.setCellValueFactory(d -> {
            String inst = d.getValue().path("instructions").asText();
            return new SimpleStringProperty(inst.isEmpty() ? "—" : inst);
        });

        roundsTable.setItems(rounds);
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
                    List<JobItem> items = new ArrayList<>();
                    array.forEach(job -> {
                        Long id = job.path("id").asLong();
                        String name = job.path("companyName").asText()
                            + " — " + job.path("roleDescription").asText();
                        jobNames.put(id, job.path("companyName").asText());
                        items.add(new JobItem(id, name));
                    });
                    Platform.runLater(() -> {
                        jobCombo.getItems().setAll(items);
                        if (!items.isEmpty()) jobCombo.setValue(items.get(0));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddRound() {
        JobItem selectedJob = jobCombo.getValue();
        String roundName = roundNameCombo.getValue();
        LocalDate date = datePicker.getValue();
        String time = timeField.getText().trim();
        String venue = venueField.getText().trim();

        // Validation
        if (selectedJob == null) {
            setFormStatus("Please select a job drive.", false);
            return;
        }
        if (roundName == null || roundName.isEmpty()) {
            setFormStatus("Please select or enter a round name.", false);
            return;
        }
        if (date == null) {
            setFormStatus("Please select a date.", false);
            return;
        }
        if (time.isEmpty()) {
            setFormStatus("Please enter a time (HH:MM).", false);
            return;
        }
        if (venue.isEmpty()) {
            setFormStatus("Please enter a venue or meeting link.", false);
            return;
        }

        // Parse time
        LocalDateTime scheduledTime;
        try {
            String[] parts = time.split(":");
            scheduledTime = date.atTime(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1])
            );
        } catch (Exception e) {
            setFormStatus("Invalid time format. Use HH:MM (e.g. 10:30).", false);
            return;
        }

        addBtn.setDisable(true);
        setFormStatus("Scheduling round...", true);

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("roundName", roundName);
            body.put("scheduledTime", scheduledTime.toString());
            body.put("venueOrLink", venue);
            body.put("instructions", instructionsField.getText().trim());
            body.put("jobPostId", selectedJob.jobId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(body);

            new Thread(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/rounds"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                    HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        addBtn.setDisable(false);
                        if (response.statusCode() == 200) {
                            setFormStatus("✅ Round scheduled successfully!", true);
                            handleClear();
                            handleLoadRounds();
                        } else {
                            setFormStatus("❌ Error: " + response.body(), false);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addBtn.setDisable(false);
                        setFormStatus("❌ Cannot connect to server.", false);
                    });
                }
            }).start();

        } catch (Exception e) {
            addBtn.setDisable(false);
            setFormStatus("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleLoadRounds() {
        JobItem selected = jobCombo.getValue();
        if (selected == null) {
            tableStatus.setText("Please select a job first.");
            return;
        }

        tableStatus.setText("Loading rounds...");
        rounds.clear();

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/rounds/job/" + selected.jobId))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());
                    List<JsonNode> roundList = new ArrayList<>();
                    array.forEach(roundList::add);

                    Platform.runLater(() -> {
                        rounds.setAll(roundList);
                        tableStatus.setText(roundList.isEmpty()
                            ? "No rounds scheduled yet for this job."
                            : roundList.size() + " round(s) found.");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                    tableStatus.setText("Error loading rounds."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleClear() {
        roundNameCombo.setValue(null);
        datePicker.setValue(null);
        timeField.clear();
        venueField.clear();
        instructionsField.clear();
        formStatus.setText(" ");
    }

    private void setFormStatus(String message, boolean success) {
        formStatus.setText(message);
        formStatus.setStyle(success
            ? "-fx-text-fill:#38a169; -fx-font-size:12px;"
            : "-fx-text-fill:#e94560; -fx-font-size:12px;");
    }

    // Inner class
    public static class JobItem {
        public final Long jobId;
        public final String label;

        public JobItem(Long jobId, String label) {
            this.jobId = jobId;
            this.label = label;
        }

        @Override
        public String toString() { return label; }
    }
}