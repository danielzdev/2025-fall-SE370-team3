package csusm.cougarplanner.controllers;

import csusm.cougarplanner.Launcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    private double labelParentCenter;

    public void setText(String course) {
        courseLabel.setText(course);

        Platform.runLater(() -> {
            courseLabel.setLayoutX(labelParentCenter - (courseLabel.getWidth() / 2));
        });
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

    public FXMLLoader getFXMLLoader() {
        String FXMLPath = "AssignmentsCourseHeaderComponent.fxml";
        return new FXMLLoader(Launcher.class.getResource(FXMLPath));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setHeight(25);

        Platform.runLater(() -> {
            labelParentCenter = labelParent.getWidth() / 2; //evaluate the center of the node after it is rendered
        });
    }
}
