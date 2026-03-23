package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageJobsController implements AdminChildController {

    @FXML private TableView<JsonNode> jobTable;
    @FXML private TableColumn<JsonNode, String> colCompany;
    @FXML private TableColumn<JsonNode, String> colRole;
    @FXML private TableColumn<JsonNode, String> colPackage;
    @FXML private TableColumn<JsonNode, String> colDeadline;
    @FXML private TableColumn<JsonNode, String> colStatus;
    @FXML private TableColumn<JsonNode, String> colBranches;

    @FXML private TableView<StudentRow> studentTable;
    @FXML private TableColumn<StudentRow, Boolean> colSelect;
    @FXML private TableColumn<StudentRow, String> colUsn;
    @FXML private TableColumn<StudentRow, String> colName;
    @FXML private TableColumn<StudentRow, String> colBranch;
    @FXML private TableColumn<StudentRow, String> colCgpa;
    @FXML private TableColumn<StudentRow, String> colBacklogs;
    @FXML private TableColumn<StudentRow, String> colAppStatus;

    @FXML private Label selectLabel;
    @FXML private Label eligibleTitle;
    @FXML private Label shortlistStatus;

    private AdminDashboardController parentController;
    private final ObservableList<StudentRow> studentRows = FXCollections.observableArrayList();
    private Long currentJobId = null;

    @Override
    public void setParentController(AdminDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        // Jobs table
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

        colBranches.setCellValueFactory(d -> {
            JsonNode branches = d.getValue().path("allowedBranches");
            List<String> list = new ArrayList<>();
            branches.forEach(b -> list.add(b.asText()));
            return new SimpleStringProperty(String.join(", ", list));
        });

        // Students table
        colSelect.setCellValueFactory(d -> d.getValue().selectedProperty());
        colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
        colSelect.setEditable(true);
        studentTable.setEditable(true);

        colUsn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().usn));
        colName.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().name));
        colBranch.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().branch));
        colCgpa.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().cgpa));
        colBacklogs.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().backlogs));

        // App status with color
        colAppStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().appStatus));
        colAppStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(formatStatus(status));
                setStyle(switch (status) {
                    case "APPLIED"             -> "-fx-text-fill:#3182ce; -fx-font-weight:bold;";
                    case "SHORTLISTED_OA"      -> "-fx-text-fill:#d69e2e; -fx-font-weight:bold;";
                    case "OA_CLEARED"          -> "-fx-text-fill:#805ad5; -fx-font-weight:bold;";
                    case "INTERVIEW_SCHEDULED" -> "-fx-text-fill:#2b6cb0; -fx-font-weight:bold;";
                    case "OFFERED"             -> "-fx-text-fill:#276749; -fx-font-weight:bold;";
                    case "REJECTED"            -> "-fx-text-fill:#9b2335; -fx-font-weight:bold;";
                    case "NOT APPLIED"         -> "-fx-text-fill:#a0aec0;";
                    default                    -> "";
                });
            }
        });

        studentTable.setItems(studentRows);
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
    private void handleViewEligible() {
        JsonNode selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selectLabel.setText("← Please select a job first");
            return;
        }

        currentJobId = selected.path("id").asLong();
        String company = selected.path("companyName").asText();
        String jobStatus = selected.path("status").asText();
        eligibleTitle.setText("Applicants — " + company + " [" + jobStatus + "]");
        shortlistStatus.setText("Loading...");
        shortlistStatus.setStyle("-fx-text-fill:#3182ce; -fx-font-size:12px;");
        studentRows.clear();

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                ObjectMapper mapper = new ObjectMapper();

                // Get applications for this job
                HttpRequest appRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/applications/job/" + currentJobId))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();
                HttpResponse<String> appResponse = client.send(
                    appRequest, HttpResponse.BodyHandlers.ofString());

                // Map studentId -> {appId, appStatus}
                Map<Long, Long> studentToAppId = new HashMap<>();
                Map<Long, String> studentToStatus = new HashMap<>();

                if (appResponse.statusCode() == 200) {
                    JsonNode apps = mapper.readTree(appResponse.body());
                    apps.forEach(app -> {
                        Long sid = app.path("studentId").asLong();
                        studentToAppId.put(sid, app.path("id").asLong());
                        studentToStatus.put(sid, app.path("currentStatus").asText());
                    });
                }

                // Get eligible students
                HttpRequest eligRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/jobs/" + currentJobId + "/eligible-students"))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();
                HttpResponse<String> eligResponse = client.send(
                    eligRequest, HttpResponse.BodyHandlers.ofString());

                if (eligResponse.statusCode() == 200) {
                    JsonNode eligArr = mapper.readTree(eligResponse.body());
                    List<StudentRow> rows = new ArrayList<>();

                    eligArr.forEach(s -> {
                        Long sid = s.path("studentId").asLong();
                        Long appId = studentToAppId.get(sid);
                        String appStatus = studentToStatus.getOrDefault(sid, "NOT APPLIED");
                        rows.add(new StudentRow(
                            sid, appId, appStatus,
                            s.path("usn").asText(),
                            s.path("firstName").asText() + " " + s.path("lastName").asText(),
                            s.path("branch").asText(),
                            s.path("currentCgpa").asText(),
                            String.valueOf(s.path("activeBacklogs").asInt())
                        ));
                    });

                    Platform.runLater(() -> {
                        studentRows.setAll(rows);
                        long applied = rows.stream()
                            .filter(r -> r.appId != null).count();
                        shortlistStatus.setText(rows.size() + " eligible, "
                            + applied + " applied. Select applied students to shortlist.");
                        shortlistStatus.setStyle("-fx-text-fill:#718096; -fx-font-size:12px;");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    shortlistStatus.setText("Error loading applicants.");
                    shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
                });
                e.printStackTrace();
            }
        }).start();
    }

    // Close applications — OPEN -> ONGOING
    @FXML
    private void handleCloseApplications() {
        updateJobStatus("ONGOING",
            "Close Applications",
            "This will stop students from applying. Move to ONGOING phase?",
            "🔒 Applications closed! Job is now ONGOING.");
    }

    // Move to OA phase — ONGOING stays, but we notify
    @FXML
    private void handleMoveToOA() {
        updateJobStatus("ONGOING",
            "Move to OA Phase",
            "Confirm moving this job to Online Assessment phase?",
            "📝 Job moved to OA phase!");
    }

    // Mark results out
    @FXML
    private void handleResultsOut() {
        updateJobStatus("RESULTS_OUT",
            "Results Out",
            "Mark this job drive as Results Out?",
            "🏁 Results marked as out!");
    }

    private void updateJobStatus(String newStatus, String title,
                                  String message, String successMsg) {
        JsonNode selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selectLabel.setText("← Please select a job first");
            return;
        }

        Long jobId = selected.path("id").asLong();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(selected.path("companyName").asText() + " — " + title);
        confirm.setContentText(message);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/jobs/"
                                + jobId + "/status?status=" + newStatus))
                            .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                            .PUT(HttpRequest.BodyPublishers.noBody())
                            .build();

                        HttpResponse<String> response = client.send(
                            request, HttpResponse.BodyHandlers.ofString());

                        Platform.runLater(() -> {
                            if (response.statusCode() == 200) {
                                selectLabel.setText("✅ " + successMsg);
                                loadJobs(); // Refresh table
                            } else {
                                selectLabel.setText("❌ Error: " + response.body());
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() ->
                            selectLabel.setText("❌ Cannot connect to server."));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleShortlist() {
        List<StudentRow> selected = studentRows.stream()
            .filter(r -> r.selectedProperty().get())
            .toList();

        if (selected.isEmpty()) {
            shortlistStatus.setText("Please select at least one student.");
            shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
            return;
        }

        List<StudentRow> withApp = selected.stream()
            .filter(r -> r.appId != null)
            .toList();

        if (withApp.isEmpty()) {
            shortlistStatus.setText("❌ Selected students have not applied yet.");
            shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
            return;
        }

        shortlistStatus.setText("Shortlisting " + withApp.size() + " students...");
        shortlistStatus.setStyle("-fx-text-fill:#3182ce; -fx-font-size:12px;");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger doneCount = new AtomicInteger(0);

        for (StudentRow row : withApp) {
            new Thread(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/applications/"
                            + row.appId + "/status?status=SHORTLISTED_OA"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();

                    HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) successCount.incrementAndGet();

                    if (doneCount.incrementAndGet() == withApp.size()) {
                        Platform.runLater(() -> {
                            shortlistStatus.setText("✅ Shortlisted "
                                + successCount.get() + " student(s) for OA!");
                            shortlistStatus.setStyle("-fx-text-fill:#38a169; -fx-font-size:12px;");
                            // Refresh to show updated statuses
                            handleViewEligible();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void handleDeleteJob() {
        JsonNode selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selectLabel.setText("← Please select a job to delete");
            return;
        }

        String company = selected.path("companyName").asText();
        Long jobId = selected.path("id").asLong();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Job");
        confirm.setHeaderText("Delete \"" + company + "\"?");
        confirm.setContentText("This will permanently delete this job and all applications.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/jobs/" + jobId))
                            .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                            .DELETE().build();

                        HttpResponse<String> res = client.send(
                            request, HttpResponse.BodyHandlers.ofString());

                        Platform.runLater(() -> {
                            if (res.statusCode() == 204) {
                                selectLabel.setText("✅ Job deleted.");
                                studentRows.clear();
                                eligibleTitle.setText("Applicants");
                                currentJobId = null;
                                loadJobs();
                            } else {
                                selectLabel.setText("❌ Error: " + res.body());
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() ->
                            selectLabel.setText("❌ Cannot connect to server."));
                    }
                }).start();
            }
        });
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "APPLIED"             -> "📝 Applied";
            case "SHORTLISTED_OA"      -> "⭐ Shortlisted OA";
            case "OA_CLEARED"          -> "🎯 OA Cleared";
            case "INTERVIEW_SCHEDULED" -> "📅 Interview";
            case "OFFERED"             -> "🎉 Offered";
            case "REJECTED"            -> "❌ Rejected";
            case "NOT APPLIED"         -> "— Not Applied";
            default                    -> status;
        };
    }

    public static class StudentRow {
        private final Long studentId;
        public final Long appId;
        public final String appStatus;
        public final String usn;
        public final String name;
        public final String branch;
        public final String cgpa;
        public final String backlogs;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public StudentRow(Long studentId, Long appId, String appStatus,
                         String usn, String name, String branch,
                         String cgpa, String backlogs) {
            this.studentId = studentId;
            this.appId = appId;
            this.appStatus = appStatus;
            this.usn = usn;
            this.name = name;
            this.branch = branch;
            this.cgpa = cgpa;
            this.backlogs = backlogs;
        }

        public SimpleBooleanProperty selectedProperty() { return selected; }
        public Long getStudentId() { return studentId; }
    }
}