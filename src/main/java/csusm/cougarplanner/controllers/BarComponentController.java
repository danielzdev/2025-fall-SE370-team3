package csusm.cougarplanner.controllers;

import csusm.cougarplanner.Launcher;
import csusm.cougarplanner.util.ColorUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

import static csusm.cougarplanner.util.ColorUtil.getColorBurn;
import static csusm.cougarplanner.util.ColorUtil.toCssHex;

public class BarComponentController implements Initializable {

    @FXML
    private AnchorPane barParent;

    @FXML
    private Pane  primaryBar;

    //default conditions of primaryBar's styles, listed in the order they should be added to setStyle()
    private String primaryBarColorStyle = "-fx-background-color: #DFC1FF;";
    private String primaryBarRadius = "-fx-border-color: transparent;";
    private String primaryBarBorderColor = "-fx-background-radius: 30;";
    private String primaryBarBorderRadius = "-fx-border-radius: 30;";
    //any time any of these attributes are updated, the setStyle parameter needs to be constructed using these strings

    @FXML
    private Label barLabel;

    private final Color topColor = Color.rgb(54, 54, 54); //hex color for the top value of a color effect

    private boolean leftBlock, rightBlock;

    private void buildSetStyle() {
        primaryBar.setStyle(primaryBarColorStyle + " " + primaryBarRadius + " " + primaryBarBorderColor + " " + primaryBarBorderRadius);
    }

    public Pane getBar() {
        return primaryBar;
    }

    public Label getLabel() {
        return barLabel;
    }

    public void setVisibleParent(boolean visible) {
        barParent.setVisible(visible);
    }

    public void setVisibleSideBlocks(boolean leftBlock, boolean rightBlock) {
        //when a corner is visible, it has no radius (and thus a corner)
        int leftValue = leftBlock ? 0 : 30;
        int rightValue = rightBlock ? 0 : 30;

        this.leftBlock = leftBlock;
        this.rightBlock = rightBlock;

        //each corner of the primaryBar has its own radius value, each value needs to be passed into setStyle
        //                                              UpLeft            UpRight            BtmRight           BtmLeft
        primaryBarRadius = "-fx-background-radius: " + leftValue + " " + rightValue + " " + rightValue + " " + leftValue + ";";
        primaryBarBorderRadius = "-fx-border-radius: " + leftValue + " " + rightValue + " " + rightValue + " " + leftValue + ";";

        buildSetStyle();
    }

    public void setVisibleLabel(boolean visible) {
        barLabel.setVisible(visible);
    }

    public void setText(String text) {
        barLabel.setText(text);
    }

    public void setColor(String hexColor) {
        if (ColorUtil.validHexColor(hexColor)) { return; }

        changeColor(Color.web(hexColor));
    }

    public void setColor(Color color) {
        changeColor(color);
    }

    private void changeColor(Color color) {
        primaryBarColorStyle = "-fx-background-color: " + toCssHex(color) + ";";

        //change the color of the border to the same color as the label.
        //      The label does this automatically, however, it is impossible to retrieve
        //      the final displayed color, so here it needs to be calculated
        //String colorBurn = toCssHex(getColorBurn(color, topColor));

        //primaryBarBorderColor = "-fx-border-color: " + colorBurn + " " + ((rightBlock) ? "transparent" : colorBurn) + " " + colorBurn + " " + ((leftBlock) ? "transparent" : colorBurn) + ";";
        primaryBarBorderColor = "-fx-border-color: transparent;";

        buildSetStyle();
    }

    public void setHeight(double desiredHeight) {
        barParent.setPrefHeight(desiredHeight);

        getBar().setPrefHeight(desiredHeight);

        double fontSize = (desiredHeight + 2) * 3 / 4;
        barLabel.setFont(Font.font("Arial Rounded MT Bold", fontSize));
    }

    /**
     * returns the height of the bar node
     *      this is useful for the generation of the bar
     *      hitboxes; they need to be the same height
     *      for consistency.
     * @return bar height
     */
    public double getHeight() {
        return barParent.getPrefHeight();
    }

    public double getLayoutY() {
        return barParent.getLayoutY();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        barParent.setVisible(false);
        barLabel.setVisible(false);
        setVisibleSideBlocks(true, true);
        setHeight(15);

        primaryBar.setVisible(true);

        Platform.runLater(() -> {
            primaryBar.setEffect(new DropShadow(10, Color.web("#363636DD")));
        });
    }
}
