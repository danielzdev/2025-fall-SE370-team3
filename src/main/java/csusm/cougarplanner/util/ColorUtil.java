package csusm.cougarplanner.util;

import javafx.scene.paint.Color;

public class ColorUtil {
    /** Returns true if the input is NOT a valid 6-digit hex color string (e.g. "#FF00AA"). */
    public static boolean isInvalidHexColor(String hexColor) {
        return !hexColor.matches("#[\\dA-Fa-f]{6}");
    }

    public static Color getColorBurn(Color bottomColor, Color topColor) {
        int red, green, blue;

        red = colorBurn((int)(bottomColor.getRed() * 255),
                        (int)(topColor.getRed() * 255));
        green = colorBurn((int)(bottomColor.getGreen() * 255),
                          (int)(topColor.getGreen() * 255));
        blue = colorBurn((int)(bottomColor.getBlue() * 255),
                         (int)(topColor.getBlue() * 255));

        return Color.rgb(red, green, blue);
    }

    /**
     * Takes in two colors (defined as doubles with values between 0 and 1),
     *      layered on top of one another, that interact via a ColorBurn.
     * ColorBurn is defined as "The inverse of the bottom input color
     *      components are divided by the top input color components, all
     *      of which is then inverted to produce the resulting color." Where
     *      an 'inverse' operation is just the complement of the function
     *      input. e.g. inverse of 42 -> 255 - 42 -> 213
     * @param bottomColor bottom color component double from 0 to 255
     * @param topColor top color component double from 0 to 255
     * @return returns the calculated color burn as a value from 0 to 255
     */
    private static int colorBurn(int bottomColor, int topColor) {
        if (topColor == 0) return 0;

        int value = 255 - (255 - bottomColor) * 255 / topColor;

        //clamp
        if (value < 0) value = 0;
        if (value > 255) value = 255;

        return value;
    }

    public static String toCssHex(Color c) {
        return String.format(
                "#%02x%02x%02x",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255)
        );
    }

    /** Returns a color from a fixed 10-color palette, cycling by index. */
    public static Color getAssignmentColor(int bar) {
        String[] colorPalette = {
            "#F94144", "#F3722C", "#F8961E", "#F9844A", "#F9C74F",
            "#90BE6D", "#43AA8B", "#4D908E", "#577590", "#277DA1"
        };
        return Color.web(colorPalette[bar % colorPalette.length]);
    }
}
