package csusm.cougarplanner.controllers;

import csusm.cougarplanner.theme.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsPanelController implements Initializable {
    private static final String UNSELECTED = "option-unselected";
    private static final String SELECTED = "option-selected";
    private static final String HOVER = "option-hover";

    @FXML private AnchorPane settingsPanelRoot;

    @FXML private Label sortAnnouncementsLabel;
    @FXML private Label sortAssignmentsLabel;
    @FXML private Label sortTasksLabel;

    @FXML private Label displayWeekLabel;
    @FXML private Label displayDayLabel;

    @FXML private Label weekStartSundayLabel;
    @FXML private Label weekStartMondayLabel;

    @FXML private Label themeCougarLabel;
    @FXML private Label themeDarkLabel;
    @FXML private Label themeSunsetLabel;

    @FXML private Label closeButton;

    private MainPageController mainController;

    private Label activeSortLabel;
    private Label activeDisplayLabel;
    private Label activeWeekStartLabel;
    private Label activeThemeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyUnselected(sortAnnouncementsLabel);
        applyUnselected(sortAssignmentsLabel);
        applyUnselected(sortTasksLabel);
        applyUnselected(displayWeekLabel);
        applyUnselected(displayDayLabel);
        applyUnselected(weekStartSundayLabel);
        applyUnselected(weekStartMondayLabel);
        applyUnselected(themeCougarLabel);
        applyUnselected(themeDarkLabel);
        applyUnselected(themeSunsetLabel);
    }

    public void bindMainController(MainPageController controller) {
        this.mainController = controller;
        selectSortLabel(sortAnnouncementsLabel);
        selectDisplayLabel(controller.isWeekView() ? displayWeekLabel : displayDayLabel);
        selectWeekStartLabel(controller.isWeekStartSunday() ? weekStartSundayLabel : weekStartMondayLabel);
        selectThemeLabel(themeLabelFor(controller.getTheme()));
    }

    public void markSortSelection(String viewName) {
        switch (viewName) {
            case "Announcements" -> selectSortLabel(sortAnnouncementsLabel);
            case "Assignments" -> selectSortLabel(sortAssignmentsLabel);
            case "Tasks" -> selectSortLabel(sortTasksLabel);
        }
    }

    @FXML
    private void onSortByClicked(MouseEvent event) {
        if (!(event.getSource() instanceof Label label)) return;
        selectSortLabel(label);
    }

    public String getSelectedSortView() {
        if (activeSortLabel == null) return "Announcements";
        return activeSortLabel.getText();
    }

    @FXML
    private void onCloseClicked(MouseEvent event) {
        if (mainController == null) return;
        mainController.closeSettingsPanel();
    }

    @FXML
    private void onCloseHoverEnter(MouseEvent event) {
        closeButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);" +
                "-fx-background-radius: 4; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
    }

    @FXML
    private void onCloseHoverExit(MouseEvent event) {
        closeButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 4; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
    }

    @FXML
    private void onDisplayClicked(MouseEvent event) {
        if (!(event.getSource() instanceof Label label)) return;
        if (mainController == null) return;

        boolean wantWeek = label.getText().equals("Week");
        if (mainController.isWeekView() != wantWeek) {
            mainController.setDisplayView(wantWeek);
        }
        selectDisplayLabel(label);
    }

    @FXML
    private void onWeekStartClicked(MouseEvent event) {
        if (!(event.getSource() instanceof Label label)) return;
        if (mainController == null) return;

        boolean wantSunday = label.getText().equals("Sunday");
        if (mainController.isWeekStartSunday() != wantSunday) {
            mainController.setWeekStart(wantSunday);
        }
        selectWeekStartLabel(label);
    }

    @FXML
    private void onThemeClicked(MouseEvent event) {
        if (!(event.getSource() instanceof Label label)) return;
        if (mainController == null) return;

        String themeName = label.getText().toLowerCase();
        mainController.setTheme(themeName);
        selectThemeLabel(label);
    }

    @FXML
    private void onOptionHoverEnter(MouseEvent event) {
        if (!(event.getSource() instanceof Label label)) return;
        if (isActive(label)) return;
        applyClass(label, HOVER);
    }

    @FXML
    private void onOptionHoverExit(MouseEvent event) {
        if (!(event.getSource() instanceof Label label)) return;
        if (isActive(label)) return;
        applyUnselected(label);
    }

    private boolean isActive(Label label) {
        return label == activeSortLabel
                || label == activeDisplayLabel
                || label == activeWeekStartLabel
                || label == activeThemeLabel;
    }

    private void selectSortLabel(Label label) {
        if (activeSortLabel != null) applyUnselected(activeSortLabel);
        activeSortLabel = label;
        applySelected(label);
    }

    private void selectDisplayLabel(Label label) {
        if (activeDisplayLabel != null) applyUnselected(activeDisplayLabel);
        activeDisplayLabel = label;
        applySelected(label);
    }

    private void selectWeekStartLabel(Label label) {
        if (activeWeekStartLabel != null) applyUnselected(activeWeekStartLabel);
        activeWeekStartLabel = label;
        applySelected(label);
    }

    private void selectThemeLabel(Label label) {
        if (label == null) return;
        if (activeThemeLabel != null) applyUnselected(activeThemeLabel);
        activeThemeLabel = label;
        applySelected(label);
    }

    private Label themeLabelFor(String themeName) {
        String resolved = ThemeManager.resolve(themeName);
        return switch (resolved) {
            case ThemeManager.DARK -> themeDarkLabel;
            case ThemeManager.SUNSET -> themeSunsetLabel;
            default -> themeCougarLabel;
        };
    }

    private void applySelected(Label label) {
        applyClass(label, SELECTED);
    }

    private void applyUnselected(Label label) {
        applyClass(label, UNSELECTED);
    }

    private void applyClass(Label label, String cssClass) {
        label.getStyleClass().removeAll(UNSELECTED, SELECTED, HOVER);
        label.getStyleClass().add(cssClass);
    }
}
