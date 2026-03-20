package com.placement.controller;

import com.placement.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private Label userEmailLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnPostJob;
    @FXML private Button btnManageJobs;
    @FXML private Button btnStudents;
    @FXML private Button btnReports;

    @FXML
    public void initialize() {
        userEmailLabel.setText(SessionManager.getInstance().getEmail());
        setActive(btnDashboard);
        loadView("/com/placement/fxml/admin-home.fxml");
    }

    private void setActive(Button active) {
        Button[] all = {btnDashboard, btnPostJob, btnManageJobs, btnStudents, btnReports};
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
            if (controller instanceof AdminChildController) {
                ((AdminChildController) controller).setParentController(this);
            }
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showDashboard() {
        setActive(btnDashboard);
        loadView("/com/placement/fxml/admin-home.fxml");
    }

    @FXML
    public void showPostJob() {
        setActive(btnPostJob);
        loadView("/com/placement/fxml/post-job.fxml");
    }

    @FXML
    public void showManageJobs() {
        setActive(btnManageJobs);
        loadView("/com/placement/fxml/manage-jobs.fxml");
    }

    @FXML
    public void showStudents() {
        setActive(btnStudents);
        loadView("/com/placement/fxml/student-database.fxml");
    }

    @FXML
    public void showReports() {
        setActive(btnReports);
        loadView("/com/placement/fxml/reports.fxml");
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