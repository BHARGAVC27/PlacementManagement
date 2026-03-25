package com.placement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.*;

public class StudentDashboardController {

    @FXML private Label userEmailLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnJobs;
    @FXML private Button btnApplications;
    @FXML private Button btnInterviews;
    @FXML private Button btnProfile;

    @FXML
    public void initialize() {
        userEmailLabel.setText(SessionManager.getInstance().getEmail());
        setActive(btnDashboard);
        loadView("/com/placement/fxml/student-home.fxml");
    }

    private void setActive(Button active) {
        Button[] all = {btnDashboard, btnJobs, btnApplications, btnInterviews, btnProfile};
        for (Button b : all) {
            b.getStyleClass().removeAll("sidebar-btn-active");
            if (!b.getStyleClass().contains("sidebar-btn"))
                b.getStyleClass().add("sidebar-btn");
        }
        active.getStyleClass().add("sidebar-btn-active");
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof StudentChildController) {
                ((StudentChildController) controller).setParentController(this);
            }
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void showDashboard() {
        setActive(btnDashboard);
        loadView("/com/placement/fxml/student-home.fxml");
    }

    @FXML public void showJobDrives() {
        setActive(btnJobs);
        loadView("/com/placement/fxml/job-drives.fxml");
    }

    @FXML public void showApplications() {
        setActive(btnApplications);
        loadView("/com/placement/fxml/my-applications.fxml");
    }

    @FXML public void showInterviews() {
        setActive(btnInterviews);
        loadView("/com/placement/fxml/interview-schedule.fxml");
    }

    @FXML public void showProfile() {
        setActive(btnProfile);
        loadView("/com/placement/fxml/student-profile.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().clearSession();
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/placement/fxml/login.fxml")
            );
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}