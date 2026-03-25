package com.placement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.*;
import java.util.*;

public class PostJobController implements AdminChildController {

    @FXML private TextField companyField;
    @FXML private TextField packageField;
    @FXML private TextArea roleField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField minCgpaField;
    @FXML private TextField min10thField;
    @FXML private TextField min12thField;
    @FXML private TextField maxBacklogsField;
    @FXML private CheckBox cbCSE, cbISE, cbECE, cbMECH, cbCIVIL, cbEEE;
    @FXML private Label statusLabel;
    @FXML private Button postButton;
    @FXML private Button clearButton;

    private AdminDashboardController parentController;

    @Override
    public void setParentController(AdminDashboardController parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("OPEN", "ONGOING", "CLOSED", "RESULTS_OUT");
        statusCombo.setValue("OPEN");
    }

    @FXML
    private void handlePostJob() {
        if (companyField.getText().isEmpty() || roleField.getText().isEmpty()
                || packageField.getText().isEmpty() || deadlinePicker.getValue() == null) {
            showStatus("Please fill all required fields.", false);
            return;
        }

        List<String> branches = new ArrayList<>();
        if (cbCSE.isSelected()) branches.add("CSE");
        if (cbISE.isSelected()) branches.add("ISE");
        if (cbECE.isSelected()) branches.add("ECE");
        if (cbMECH.isSelected()) branches.add("MECH");
        if (cbCIVIL.isSelected()) branches.add("CIVIL");
        if (cbEEE.isSelected()) branches.add("EEE");

        if (branches.isEmpty()) {
            showStatus("Please select at least one branch.", false);
            return;
        }

        postButton.setDisable(true);
        clearButton.setDisable(true);
        showStatus("Posting job...", true);

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("companyName", companyField.getText().trim());
            body.put("roleDescription", roleField.getText().trim());
            body.put("packageLPA", new java.math.BigDecimal(packageField.getText().trim()));
            body.put("deadline", deadlinePicker.getValue().toString());
            body.put("status", statusCombo.getValue());
            body.put("minCgpa", new java.math.BigDecimal(
                minCgpaField.getText().isEmpty() ? "0" : minCgpaField.getText().trim()));
            body.put("min10th", new java.math.BigDecimal(
                min10thField.getText().isEmpty() ? "0" : min10thField.getText().trim()));
            body.put("min12th", new java.math.BigDecimal(
                min12thField.getText().isEmpty() ? "0" : min12thField.getText().trim()));
            body.put("maxBacklogs", Integer.parseInt(
                maxBacklogsField.getText().isEmpty() ? "0" : maxBacklogsField.getText().trim()));
            body.put("allowedBranches", branches);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(body);

            new Thread(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/jobs"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                    HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        postButton.setDisable(false);
                        clearButton.setDisable(false);
                        if (response.statusCode() == 200) {
                            showStatus("✅ Job posted! Redirecting...", true);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1000);
                                    Platform.runLater(() -> {
                                        if (parentController != null)
                                            parentController.showDashboard();
                                    });
                                } catch (InterruptedException ignored) {}
                            }).start();
                        } else {
                            showStatus("❌ Error: " + response.body(), false);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        postButton.setDisable(false);
                        clearButton.setDisable(false);
                        showStatus("❌ Cannot connect to server.", false);
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            postButton.setDisable(false);
            clearButton.setDisable(false);
            showStatus("Please enter valid numbers.", false);
        } catch (Exception e) {
            postButton.setDisable(false);
            clearButton.setDisable(false);
            showStatus("Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleClear() {
        companyField.clear();
        packageField.clear();
        roleField.clear();
        deadlinePicker.setValue(null);
        statusCombo.setValue("OPEN");
        minCgpaField.clear();
        min10thField.clear();
        min12thField.clear();
        maxBacklogsField.clear();
        cbCSE.setSelected(false);
        cbISE.setSelected(false);
        cbECE.setSelected(false);
        cbMECH.setSelected(false);
        cbCIVIL.setSelected(false);
        cbEEE.setSelected(false);
        statusLabel.setText(" ");
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
            ? "-fx-text-fill:#38a169; -fx-font-size:13px;"
            : "-fx-text-fill:#e94560; -fx-font-size:13px;");
    }
}