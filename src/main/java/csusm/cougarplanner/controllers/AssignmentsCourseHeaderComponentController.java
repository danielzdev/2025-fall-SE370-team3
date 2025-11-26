package csusm.cougarplanner.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

public class AssignmentsCourseHeaderComponentController implements Initializable {

    @FXML
    private Label courseLabel;

    @FXML
    private AnchorPane parentAnchorPane, labelParent;

    private final double labelParentCenter = (143 + 1.0 / 3) / 2.0;
    private double courseLabelCenter;

    public void setText(String course) {
        courseLabel.setText(course);

        Platform.runLater(() -> courseLabel.setLayoutX(labelParentCenter - (courseLabel.getWidth() / 2.0)));
    }

    public void setVisibleText(boolean visible) {
        courseLabel.setVisible(visible);
    }

    public void setHeight(double desiredHeight) {
        parentAnchorPane.setPrefHeight(desiredHeight);
        labelParent.setPrefHeight(desiredHeight - 3);

        double fontSize = (desiredHeight / 1.5 + 2) * 3 / 4;
        courseLabel.setFont(Font.font("Arial Rounded MT Bold", fontSize));
    }

    public double getHeight() {
        return parentAnchorPane.getHeight();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setHeight(25);
    }
}
