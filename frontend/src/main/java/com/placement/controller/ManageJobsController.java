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
        colBranches.setCellValueFactory(d -> {
            JsonNode branches = d.getValue().path("allowedBranches");
            List<String> list = new ArrayList<>();
            branches.forEach(b -> list.add(b.asText()));
            return new SimpleStringProperty(String.join(", ", list));
        });

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
        eligibleTitle.setText("Eligible Students — " + company);
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

                // Map studentId -> applicationId
                Map<Long, Long> studentToApp = new HashMap<>();
                if (appResponse.statusCode() == 200) {
                    JsonNode apps = mapper.readTree(appResponse.body());
                    apps.forEach(app -> studentToApp.put(
                        app.path("studentId").asLong(),
                        app.path("id").asLong()
                    ));
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
                        Long appId = studentToApp.get(sid);
                        rows.add(new StudentRow(
                            sid, appId,
                            s.path("usn").asText(),
                            s.path("firstName").asText() + " " + s.path("lastName").asText(),
                            s.path("branch").asText(),
                            s.path("currentCgpa").asText(),
                            String.valueOf(s.path("activeBacklogs").asInt())
                        ));
                    });

                    Platform.runLater(() -> {
                        studentRows.setAll(rows);
                        long applied = rows.stream().filter(r -> r.appId != null).count();
                        shortlistStatus.setText(rows.size() + " eligible, "
                            + applied + " applied. Check boxes to shortlist.");
                        shortlistStatus.setStyle("-fx-text-fill:#718096; -fx-font-size:12px;");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    shortlistStatus.setText("Error loading students.");
                    shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleDeleteJob() {
        JsonNode selected = jobTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selectLabel.setText("← Please select a job first");
            return;
        }

        long jobId = selected.path("id").asLong();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Job");
        confirm.setHeaderText("Delete selected job drive?");
        confirm.setContentText("This action cannot be undone.");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }

        shortlistStatus.setText("Deleting job...");
        shortlistStatus.setStyle("-fx-text-fill:#3182ce; -fx-font-size:12px;");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/jobs/" + jobId))
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                    .DELETE()
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    if (response.statusCode() == 204 || response.statusCode() == 200) {
                        shortlistStatus.setText("Job deleted successfully.");
                        shortlistStatus.setStyle("-fx-text-fill:#2f855a; -fx-font-size:12px;");
                        selectLabel.setText("← Select a job first");
                        eligibleTitle.setText("Eligible Students");
                        currentJobId = null;
                        studentRows.clear();
                        loadJobs();
                    } else {
                        shortlistStatus.setText("Delete failed (HTTP " + response.statusCode() + ").");
                        shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    shortlistStatus.setText("Error deleting job.");
                    shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
                });
            }
        }).start();
    }

    @FXML
    private void handleShortlist() {
        if (currentJobId == null) {
            shortlistStatus.setText("Select a job and load eligible students first.");
            shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
            return;
        }

        List<StudentRow> toShortlist = studentRows.stream()
            .filter(StudentRow::isSelected)
            .filter(r -> r.appId != null)
            .toList();

        if (toShortlist.isEmpty()) {
            shortlistStatus.setText("No selected students with applications found.");
            shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
            return;
        }

        shortlistStatus.setText("Updating shortlisted status...");
        shortlistStatus.setStyle("-fx-text-fill:#3182ce; -fx-font-size:12px;");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                AtomicInteger success = new AtomicInteger(0);

                for (StudentRow row : toShortlist) {
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/applications/"
                            + row.appId + "/status?status=SHORTLISTED_OA"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        success.incrementAndGet();
                    }
                }

                Platform.runLater(() -> {
                    shortlistStatus.setText("Shortlisted " + success.get() + " of "
                        + toShortlist.size() + " selected students.");
                    shortlistStatus.setStyle("-fx-text-fill:#2f855a; -fx-font-size:12px;");
                    handleViewEligible();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    shortlistStatus.setText("Error while shortlisting students.");
                    shortlistStatus.setStyle("-fx-text-fill:#e94560; -fx-font-size:12px;");
                });
            }
        }).start();
    }

    public static class StudentRow {
        private final Long studentId;
        private final Long appId;
        private final String usn;
        private final String name;
        private final String branch;
        private final String cgpa;
        private final String backlogs;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public StudentRow(Long studentId, Long appId, String usn, String name,
                          String branch, String cgpa, String backlogs) {
            this.studentId = studentId;
            this.appId = appId;
            this.usn = usn;
            this.name = name;
            this.branch = branch;
            this.cgpa = cgpa;
            this.backlogs = backlogs;
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }
    }
}