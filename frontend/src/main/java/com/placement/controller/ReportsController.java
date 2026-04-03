package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ReportsController {

    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalJobs;
    @FXML private Label lblOffered;
    @FXML private Label lblPlacementPct;

    @FXML private Label lblAvgPkg;
    @FXML private Label lblMaxPkg;
    @FXML private Label lblMinPkg;

    @FXML private Label lblTotalApplied;
    @FXML private Label lblShortlisted;
    @FXML private Label lblOACleared;
    @FXML private Label lblInterview;
    @FXML private Label lblOfferedPipeline;
    @FXML private Label lblRejected;

    @FXML private VBox companyOffersBox;
    @FXML private VBox topHiringBox;
    @FXML private VBox branchPctBox;

    @FXML private Label statusLabel;

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        loadReport();
    }

    private void loadReport() {
        statusLabel.setText("Loading analytics...");
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/reports/summary"))
                        .header("Authorization", "Bearer " + SessionManager.getInstance().getToken())
                        .GET().build();

                HttpResponse<String> response = client.send(
                        request, HttpResponse.BodyHandlers.ofString());

                System.out.println("[Reports] Status: " + response.statusCode());

                if (response.statusCode() == 200) {
                    JsonNode d = mapper.readTree(response.body());
                    Platform.runLater(() -> {
                        // Stat cards
                        lblTotalStudents.setText(d.path("totalStudents").asText("0"));
                        lblTotalJobs.setText(d.path("totalJobs").asText("0"));
                        lblOffered.setText(d.path("totalOffered").asText("0"));
                        lblPlacementPct.setText(d.path("placementPercent").asText("0") + "%");

                        // Package stats
                        lblAvgPkg.setText(d.path("avgPackageLPA").asText("0") + " LPA");
                        lblMaxPkg.setText(d.path("maxPackageLPA").asText("0") + " LPA");
                        lblMinPkg.setText(d.path("minPackageLPA").asText("0") + " LPA");

                        // Pipeline
                        lblTotalApplied.setText(d.path("totalApplied").asText("0"));
                        lblShortlisted.setText(d.path("totalShortlisted").asText("0"));
                        lblOACleared.setText(d.path("totalOACleared").asText("0"));
                        lblInterview.setText(d.path("totalInterview").asText("0"));
                        lblOfferedPipeline.setText(d.path("totalOffered").asText("0"));
                        lblRejected.setText(d.path("totalRejected").asText("0"));

                        // Company-wise offers
                        companyOffersBox.getChildren().clear();
                        JsonNode companies = d.path("companyOffers");
                        companies.fields().forEachRemaining(e ->
                                companyOffersBox.getChildren().add(
                                        makeRow(e.getKey(), e.getValue().asText() + " offer(s)", "#2b6cb0")));
                        if (companyOffersBox.getChildren().isEmpty())
                            companyOffersBox.getChildren().add(new Label("No offers recorded yet."));

                        // Top hiring companies
                        topHiringBox.getChildren().clear();
                        JsonNode topCompanies = d.path("topHiringCompanies");
                        topCompanies.fields().forEachRemaining(e ->
                                topHiringBox.getChildren().add(
                                        makeRow(e.getKey(), e.getValue().asText() + " drive(s)", "#276749")));
                        if (topHiringBox.getChildren().isEmpty())
                            topHiringBox.getChildren().add(new Label("No companies posted yet."));

                        // Branch placement %
                        branchPctBox.getChildren().clear();
                        JsonNode branches = d.path("branchPlacementPct");
                        branches.fields().forEachRemaining(e ->
                                branchPctBox.getChildren().add(
                                        makeRow(e.getKey(), e.getValue().asText(), "#c05621")));
                        if (branchPctBox.getChildren().isEmpty())
                            branchPctBox.getChildren().add(new Label("No branch data yet."));

                        statusLabel.setText("");
                    });
                } else {
                    Platform.runLater(() ->
                            statusLabel.setText("Failed to load report (Error " + response.statusCode() + ")"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Error connecting to server."));
            }
        }).start();
    }

    private HBox makeRow(String label, String value, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#4a5568; -fx-font-size:13px; -fx-pref-width:180;");
        Label val = new Label(value);
        val.setStyle("-fx-font-weight:bold; -fx-text-fill:" + color + "; -fx-font-size:13px;");
        HBox row = new HBox(lbl, val);
        row.setStyle("-fx-padding:4 0;");
        return row;
    }
}
