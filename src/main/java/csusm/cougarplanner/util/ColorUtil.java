package csusm.cougarplanner.util;

import javafx.scene.paint.Color;

public class ColorUtil {
    public static boolean validHexColor(String hexColor) {
        //true if hexColor is actually a hex color
        return hexColor.matches("#[\\dA-Fa-f]{6}");
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

    public static Color getAssignmentColor(int bar) {
        String[] colorCodes = new String[] {"100","110","010","011","001","101","100"};

        bar = bar % 7;

        //prev: (195, 255), (123, 255), (0, 143), (74, 255)
        int red = (colorCodes[bar].charAt(0) == '0') ? 100 : 255;
        int green = (colorCodes[bar].charAt(1) == '0') ? 100 : 255;
        int blue = (colorCodes[bar].charAt(2) == '0') ? 100 : 255;

        return Color.rgb(red, green, blue);
    }
}
