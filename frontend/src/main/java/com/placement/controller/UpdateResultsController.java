package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UpdateResultsController implements AdminChildController {

    @FXML private ComboBox<JobItem> jobCombo;
    @FXML private HBox pipelineBar;
    @FXML private VBox stageCard;
    @FXML private Label stageTitle;
    @FXML private Label stageBadge;
    @FXML private Label stageDesc;
    @FXML private VBox roundDetailsBox;
    @FXML private Label roundName;
    @FXML private Label roundTime;
    @FXML private Label roundVenue;
    @FXML private Label roundInstructions;
    @FXML private TableView<ApplicantRow> studentsTable;
    @FXML private TableColumn<ApplicantRow, String> colName;
    @FXML private TableColumn<ApplicantRow, String> colUsn;
    @FXML private TableColumn<ApplicantRow, String> colBranch;
    @FXML private TableColumn<ApplicantRow, String> colCgpa;
    @FXML private TableColumn<ApplicantRow, String> colStatus;
    @FXML private TableColumn<ApplicantRow, String> colAction;
    @FXML private Label actionStatus;

    private AdminDashboardController parentController;
    private final ObservableList<ApplicantRow> rows =
        FXCollections.observableArrayList();
    private final DateTimeFormatter fmt =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private static final List<String> PIPELINE = List.of(
        "APPLIED", "SHORTLISTED_OA", "OA_CLEARED",
        "INTERVIEW_SCHEDULED", "OFFERED"
    );

    @Override
    public void setParentController(AdminDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadJobs();
        roundDetailsBox.setVisible(false);
        roundDetailsBox.setManaged(false);
    }

    private void setupTable() {
        colName.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().name));
        colUsn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().usn));
        colBranch.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().branch));
        colCgpa.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().cgpa));

        colStatus.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().currentStatus));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null); setStyle(""); return;
                }
                setText(formatStatus(s));
                setStyle(getStatusStyle(s));
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ApplicantRow row =
                    getTableView().getItems().get(getIndex());
                setGraphic(buildActionButtons(row));
            }
        });

        studentsTable.setItems(rows);
    }

    private HBox buildActionButtons(ApplicantRow row) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        switch (row.currentStatus) {
            case "APPLIED" ->
                box.getChildren().add(
                    infoLabel("Shortlist from Manage Jobs first"));
            case "SHORTLISTED_OA" ->
                box.getChildren().addAll(
                    makeBtn("✅ OA Cleared", "#c6f6d5", "#276749",
                        row, "OA_CLEARED"),
                    makeBtn("❌ Reject", "#fed7d7", "#9b2335",
                        row, "REJECTED"));
            case "OA_CLEARED" ->
                box.getChildren().addAll(
                    makeBtn("📅 Move to Interview", "#bee3f8", "#2b6cb0",
                        row, "INTERVIEW_SCHEDULED"),
                    makeBtn("❌ Reject", "#fed7d7", "#9b2335",
                        row, "REJECTED"));
            case "INTERVIEW_SCHEDULED" ->
                box.getChildren().addAll(
                    makeBtn("🎉 Offer", "#c6f6d5", "#276749",
                        row, "OFFERED"),
                    makeBtn("❌ Reject", "#fed7d7", "#9b2335",
                        row, "REJECTED"));
            case "OFFERED" ->
                box.getChildren().add(infoLabel("🎉 Placed!"));
            case "REJECTED" ->
                box.getChildren().add(infoLabel("❌ Rejected"));
            default ->
                box.getChildren().add(infoLabel("—"));
        }
        return box;
    }

    private Button makeBtn(String text, String bg, String fg,
                            ApplicantRow row, String newStatus) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color:" + bg + ";"
            + "-fx-text-fill:" + fg + ";"
            + "-fx-background-radius:4;"
            + "-fx-padding:4 10 4 10;"
            + "-fx-cursor:hand;"
            + "-fx-font-size:11px;"
            + "-fx-font-weight:bold;");
        btn.setOnAction(e -> updateStudentStatus(row, newStatus));
        return btn;
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#718096; -fx-font-size:11px;");
        return l;
    }

    private void loadJobs() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/jobs"))
                    .header("Authorization",
                        "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode array = mapper.readTree(response.body());
                    List<JobItem> items = new ArrayList<>();
                    array.forEach(job -> items.add(new JobItem(
                        job.path("id").asLong(),
                        job.path("companyName").asText()
                            + " — " + job.path("status").asText()
                    )));
                    Platform.runLater(() -> {
                        jobCombo.getItems().setAll(items);
                        if (!items.isEmpty())
                            jobCombo.setValue(items.get(0));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleLoadPipeline() {
        JobItem selected = jobCombo.getValue();
        if (selected == null) {
            actionStatus.setText("Please select a job first.");
            return;
        }

        actionStatus.setText("Loading pipeline...");
        rows.clear();

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                ObjectMapper mapper = new ObjectMapper();

                // Load applications
                HttpRequest appReq = HttpRequest.newBuilder()
                    .uri(URI.create(
                        "http://localhost:8080/api/applications/job/"
                        + selected.jobId))
                    .header("Authorization",
                        "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();
                HttpResponse<String> appRes = client.send(
                    appReq, HttpResponse.BodyHandlers.ofString());

                // Load rounds
                HttpRequest roundReq = HttpRequest.newBuilder()
                    .uri(URI.create(
                        "http://localhost:8080/api/rounds/job/"
                        + selected.jobId))
                    .header("Authorization",
                        "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();
                HttpResponse<String> roundRes = client.send(
                    roundReq, HttpResponse.BodyHandlers.ofString());

                // Load eligible students for profile details
                HttpRequest eligReq = HttpRequest.newBuilder()
                    .uri(URI.create(
                        "http://localhost:8080/api/jobs/"
                        + selected.jobId + "/eligible-students"))
                    .header("Authorization",
                        "Bearer " + SessionManager.getInstance().getToken())
                    .GET().build();
                HttpResponse<String> eligRes = client.send(
                    eligReq, HttpResponse.BodyHandlers.ofString());

                if (appRes.statusCode() == 200) {
                    JsonNode apps = mapper.readTree(appRes.body());
                    JsonNode roundsArr = roundRes.statusCode() == 200
                        ? mapper.readTree(roundRes.body())
                        : mapper.createArrayNode();

                    // Profile map
                    Map<Long, JsonNode> profileMap = new HashMap<>();
                    if (eligRes.statusCode() == 200) {
                        mapper.readTree(eligRes.body()).forEach(e ->
                            profileMap.put(
                                e.path("studentId").asLong(), e));
                    }

                    // exactCounts — where each student is RIGHT NOW
                    Map<String, Integer> exactCounts = new LinkedHashMap<>();
                    for (String s : PIPELINE) exactCounts.put(s, 0);
                    exactCounts.put("REJECTED", 0);

                    // funnelCounts — highest stage each student reached
                    // NEVER decreases even after rejection
                    Map<String, Integer> funnelCounts = new LinkedHashMap<>();
                    for (String s : PIPELINE) funnelCounts.put(s, 0);
                    funnelCounts.put("REJECTED", 0);

                    List<ApplicantRow> rowList = new ArrayList<>();

                    apps.forEach(app -> {
                        Long sid = app.path("studentId").asLong();
                        JsonNode profile = profileMap.get(sid);
                        String status = app.path("currentStatus").asText();

                        // remarks field is now returned from API
                        String remarks = app.path("remarks").asText();

                        // Track exact current position
                        exactCounts.merge(status, 1, Integer::sum);

                        if (!"REJECTED".equals(status)) {
                            // Not rejected — count in all stages reached
                            int currentIndex = PIPELINE.indexOf(status);
                            for (int pi = 0;
                                    pi <= currentIndex
                                    && pi < PIPELINE.size(); pi++) {
                                funnelCounts.merge(
                                    PIPELINE.get(pi), 1, Integer::sum);
                            }
                        } else {
                            // Rejected — use remarks to find highest
                            // stage they reached before rejection
                            // remarks = "REJECTED_FROM_SHORTLISTED_OA" etc.
                            int highestReached =
                                PIPELINE.indexOf("APPLIED");

                            if (remarks.contains("INTERVIEW_SCHEDULED")) {
                                highestReached =
                                    PIPELINE.indexOf("INTERVIEW_SCHEDULED");
                            } else if (remarks.contains("OA_CLEARED")) {
                                highestReached =
                                    PIPELINE.indexOf("OA_CLEARED");
                            } else if (remarks.contains("SHORTLISTED_OA")) {
                                highestReached =
                                    PIPELINE.indexOf("SHORTLISTED_OA");
                            }

                            // Count in all stages up to highest reached
                            for (int pi = 0;
                                    pi <= highestReached
                                    && pi < PIPELINE.size(); pi++) {
                                funnelCounts.merge(
                                    PIPELINE.get(pi), 1, Integer::sum);
                            }

                            // Also count in rejected
                            funnelCounts.merge("REJECTED", 1,
                                Integer::sum);
                        }

                        rowList.add(new ApplicantRow(
                            app.path("id").asLong(), sid,
                            profile != null
                                ? profile.path("firstName").asText()
                                    + " "
                                    + profile.path("lastName").asText()
                                : "Student #" + sid,
                            profile != null
                                ? profile.path("usn").asText() : "—",
                            profile != null
                                ? profile.path("branch").asText() : "—",
                            profile != null
                                ? profile.path("currentCgpa").asText()
                                : "—",
                            status
                        ));
                    });

                    // Active = earliest stage with students pending
                    String activeStage =
                        determineActiveStage(exactCounts);

                    // Find matching round
                    JsonNode matchingRound =
                        findRoundForStage(roundsArr, activeStage);

                    Platform.runLater(() -> {
                        buildPipelineBar(funnelCounts, activeStage);

                        if (matchingRound != null) {
                            roundDetailsBox.setVisible(true);
                            roundDetailsBox.setManaged(true);
                            roundName.setText("📋 "
                                + matchingRound
                                    .path("roundName").asText());
                            try {
                                LocalDateTime dt = LocalDateTime.parse(
                                    matchingRound
                                        .path("scheduledTime").asText());
                                roundTime.setText(
                                    "🕐 " + dt.format(fmt));
                            } catch (Exception ex) {
                                roundTime.setText("");
                            }
                            roundVenue.setText("📍 "
                                + matchingRound
                                    .path("venueOrLink").asText());
                            String inst = matchingRound
                                .path("instructions").asText();
                            roundInstructions.setText(
                                inst.isEmpty() ? "" : "📝 " + inst);
                        } else {
                            roundDetailsBox.setVisible(false);
                            roundDetailsBox.setManaged(false);
                        }

                        stageTitle.setText(getStageName(activeStage));
                        stageBadge.setText(
                            rowList.size() + " applicants total");
                        stageDesc.setText(getStageDescription(
                            activeStage, exactCounts));

                        rows.setAll(rowList.stream()
                            .sorted(Comparator.comparingInt(r -> {
                                int idx =
                                    PIPELINE.indexOf(r.currentStatus);
                                return idx == -1 ? 999 : -idx;
                            }))
                            .toList());

                        int rejected =
                            exactCounts.getOrDefault("REJECTED", 0);
                        int offered =
                            exactCounts.getOrDefault("OFFERED", 0);
                        actionStatus.setText(
                            rowList.size() + " total | "
                            + offered + " offered | "
                            + rejected + " rejected");
                        actionStatus.setStyle(
                            "-fx-text-fill:#718096;"
                            + "-fx-font-size:12px;");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    actionStatus.setText("Error loading pipeline.");
                    actionStatus.setStyle(
                        "-fx-text-fill:#e94560; -fx-font-size:12px;");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void buildPipelineBar(Map<String, Integer> funnelCounts,
                                   String active) {
        pipelineBar.getChildren().clear();

        String[] stages = {
            "APPLIED", "SHORTLISTED_OA", "OA_CLEARED",
            "INTERVIEW_SCHEDULED", "OFFERED"
        };
        String[] labels = {
            "Applied", "OA Shortlisted", "OA Cleared",
            "Interview", "Offered"
        };

        for (int i = 0; i < stages.length; i++) {
            String stage = stages[i];
            String label = labels[i];
            int count = funnelCounts.getOrDefault(stage, 0);

            boolean isActive = stage.equals(active);
            boolean isCompleted =
                PIPELINE.indexOf(stage) < PIPELINE.indexOf(active);
            boolean isOffered =
                "OFFERED".equals(stage) && count > 0;

            VBox box = new VBox(4);
            box.setAlignment(Pos.CENTER);

            if (isOffered) {
                box.setStyle(
                    "-fx-padding:10 18 10 18;"
                    + "-fx-background-radius:8;"
                    + "-fx-background-color:#c6f6d5;");
            } else if (isActive) {
                box.setStyle(
                    "-fx-padding:10 18 10 18;"
                    + "-fx-background-radius:8;"
                    + "-fx-background-color:#0f3460;"
                    + "-fx-effect:dropshadow(gaussian,"
                        + "rgba(0,0,0,0.25),8,0,0,2);");
            } else if (isCompleted) {
                box.setStyle(
                    "-fx-padding:10 18 10 18;"
                    + "-fx-background-radius:8;"
                    + "-fx-background-color:#ebf8ff;");
            } else {
                box.setStyle(
                    "-fx-padding:10 18 10 18;"
                    + "-fx-background-radius:8;"
                    + "-fx-background-color:#f7fafc;");
            }

            Label countLabel = new Label(String.valueOf(count));
            countLabel.setStyle(
                "-fx-font-size:22px; -fx-font-weight:bold;"
                + (isOffered ? "-fx-text-fill:#276749;"
                   : isActive ? "-fx-text-fill:white;"
                   : isCompleted ? "-fx-text-fill:#2b6cb0;"
                   : "-fx-text-fill:#cbd5e0;"));

            Label nameLabel = new Label(label);
            nameLabel.setStyle("-fx-font-size:10px;"
                + (isOffered ? "-fx-text-fill:#38a169;"
                   : isActive ? "-fx-text-fill:#90cdf4;"
                   : isCompleted ? "-fx-text-fill:#63b3ed;"
                   : "-fx-text-fill:#a0aec0;"));

            box.getChildren().addAll(countLabel, nameLabel);
            pipelineBar.getChildren().add(box);

            if (i < stages.length - 1) {
                Label arrow = new Label(" → ");
                arrow.setStyle(
                    "-fx-text-fill:#cbd5e0; -fx-font-size:16px;");
                pipelineBar.getChildren().add(arrow);
            }
        }

        // Rejected shown separately
        int rejected = funnelCounts.getOrDefault("REJECTED", 0);
        if (rejected > 0) {
            Label sep = new Label("    |    ");
            sep.setStyle(
                "-fx-text-fill:#e2e8f0; -fx-font-size:18px;");
            VBox rejBox = new VBox(4);
            rejBox.setAlignment(Pos.CENTER);
            rejBox.setStyle(
                "-fx-padding:10 18 10 18;"
                + "-fx-background-color:#fff5f5;"
                + "-fx-background-radius:8;");
            Label rc = new Label(String.valueOf(rejected));
            rc.setStyle(
                "-fx-font-size:22px; -fx-font-weight:bold;"
                + "-fx-text-fill:#e94560;");
            Label rl = new Label("Rejected");
            rl.setStyle(
                "-fx-font-size:10px; -fx-text-fill:#e94560;");
            rejBox.getChildren().addAll(rc, rl);
            pipelineBar.getChildren().addAll(sep, rejBox);
        }
    }

    private String determineActiveStage(
            Map<String, Integer> exactCounts) {
        for (String stage : PIPELINE) {
            if (!"OFFERED".equals(stage)
                    && exactCounts.getOrDefault(stage, 0) > 0) {
                return stage;
            }
        }
        if (exactCounts.getOrDefault("OFFERED", 0) > 0)
            return "OFFERED";
        return "APPLIED";
    }

    private JsonNode findRoundForStage(JsonNode rounds, String stage) {
        if (rounds == null || !rounds.isArray() || rounds.isEmpty())
            return null;
        String keyword = switch (stage) {
            case "SHORTLISTED_OA", "OA_CLEARED" -> "OA";
            case "INTERVIEW_SCHEDULED"           -> "Technical";
            default                              -> null;
        };
        if (keyword == null) return null;
        for (JsonNode r : rounds) {
            if (r.path("roundName").asText().contains(keyword))
                return r;
        }
        return rounds.get(0);
    }

    private String getStageName(String stage) {
        return switch (stage) {
            case "APPLIED"             -> "📝 Applications Received";
            case "SHORTLISTED_OA"      -> "⭐ OA Round — Mark Results";
            case "OA_CLEARED"          ->
                "🎯 OA Cleared — Move to Interview";
            case "INTERVIEW_SCHEDULED" ->
                "📅 Interview Round — Final Decisions";
            case "OFFERED"             -> "🎉 Placement Complete";
            default                    -> stage;
        };
    }

    private String getStageDescription(String stage,
            Map<String, Integer> exactCounts) {
        return switch (stage) {
            case "APPLIED" ->
                exactCounts.get("APPLIED")
                + " student(s) applied. "
                + "Go to Manage Jobs to shortlist for OA.";
            case "SHORTLISTED_OA" ->
                exactCounts.get("SHORTLISTED_OA")
                + " student(s) shortlisted. "
                + "Mark who cleared the OA below.";
            case "OA_CLEARED" ->
                exactCounts.get("OA_CLEARED")
                + " student(s) cleared OA. "
                + "Move them to Interview round.";
            case "INTERVIEW_SCHEDULED" ->
                exactCounts.get("INTERVIEW_SCHEDULED")
                + " student(s) in interview. "
                + "Make final offer or reject decisions.";
            case "OFFERED" ->
                exactCounts.get("OFFERED")
                + " student(s) offered! "
                + "Placement drive complete.";
            default -> "";
        };
    }

    private void updateStudentStatus(ApplicantRow row,
                                      String newStatus) {
        actionStatus.setText("Updating " + row.name + "...");
        actionStatus.setStyle(
            "-fx-text-fill:#3182ce; -fx-font-size:12px;");

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                // Store previous stage in remarks when rejecting
                // This is how funnel knows where they were rejected from
                String url =
                    "http://localhost:8080/api/applications/"
                    + row.appId + "/status?status=" + newStatus;
                if ("REJECTED".equals(newStatus)) {
                    url += "&remarks=REJECTED_FROM_"
                        + row.currentStatus;
                }

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization",
                        "Bearer "
                        + SessionManager.getInstance().getToken())
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

                HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        actionStatus.setText("✅ " + row.name
                            + " → " + formatStatus(newStatus));
                        actionStatus.setStyle(
                            "-fx-text-fill:#38a169;"
                            + "-fx-font-size:12px;");
                        handleLoadPipeline();
                    } else {
                        actionStatus.setText(
                            "❌ Error updating status.");
                        actionStatus.setStyle(
                            "-fx-text-fill:#e94560;"
                            + "-fx-font-size:12px;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    actionStatus.setText(
                        "❌ Cannot connect to server.");
                    actionStatus.setStyle(
                        "-fx-text-fill:#e94560;"
                        + "-fx-font-size:12px;");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private String formatStatus(String s) {
        return switch (s) {
            case "APPLIED"             -> "📝 Applied";
            case "SHORTLISTED_OA"      -> "⭐ Shortlisted OA";
            case "OA_CLEARED"          -> "🎯 OA Cleared";
            case "INTERVIEW_SCHEDULED" -> "📅 Interview Scheduled";
            case "OFFERED"             -> "🎉 Offered";
            case "REJECTED"            -> "❌ Rejected";
            default                    -> s;
        };
    }

    private String getStatusStyle(String s) {
        return switch (s) {
            case "APPLIED"             ->
                "-fx-text-fill:#3182ce; -fx-font-weight:bold;";
            case "SHORTLISTED_OA"      ->
                "-fx-text-fill:#d69e2e; -fx-font-weight:bold;";
            case "OA_CLEARED"          ->
                "-fx-text-fill:#805ad5; -fx-font-weight:bold;";
            case "INTERVIEW_SCHEDULED" ->
                "-fx-text-fill:#2b6cb0; -fx-font-weight:bold;";
            case "OFFERED"             ->
                "-fx-text-fill:#276749; -fx-font-weight:bold;";
            case "REJECTED"            ->
                "-fx-text-fill:#9b2335; -fx-font-weight:bold;";
            default -> "";
        };
    }

    public static class JobItem {
        public final Long jobId;
        public final String label;
        public JobItem(Long jobId, String label) {
            this.jobId = jobId;
            this.label = label;
        }
        @Override public String toString() { return label; }
    }

    public static class ApplicantRow {
        public final Long appId;
        public final Long studentId;
        public final String name;
        public final String usn;
        public final String branch;
        public final String cgpa;
        public String currentStatus;

        public ApplicantRow(Long appId, Long studentId,
                            String name, String usn,
                            String branch, String cgpa,
                            String currentStatus) {
            this.appId = appId;
            this.studentId = studentId;
            this.name = name;
            this.usn = usn;
            this.branch = branch;
            this.cgpa = cgpa;
            this.currentStatus = currentStatus;
        }
    }
}