package csusm.cougarplanner.util;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

public class TextSizingUtil {
    /*private final double[] lowercaseWidthPercentage = new double[] { 0.589, 0.589, 0.53, 0.589, 0.589, 0.294, 0.589, 0.589, 0.235, 0.235, 0.53, 0.235, 0.884, 0.589, 0.589, 0.589, 0.589, 0.355, 0.53, 0.294, 0.589, 0.53, 0.765, 0.53, 0.53, 0.53};
    private final double[] uppercaseWidthPercentage = new double[] { 0.707, 0.707, 0.765, 0.765, 0.707, 0.647, 0.824, 0.765, 0.294, 0.53, 0.707, 0.589, 0.884, 0.765, 0.824, 0.707, 0.824, 0.765, 0.707, 0.647, 0.765, 0.707, 1, 0.707, 0.707, 0.647};
    private final double[] uppercaseWPixelWidth = new double[] { 5.66, 6.61, 7.08, 7.55, 8.49, 9.44, 9.91, 10.38, 11.33, 12.27, 12.74, 13.21, 13.69, 14.16, 15.1, 16.05, 16.99, 18.88, 20.76, 22.65, 24.54, 25.48, 26.43, 27.37, 28.32, 30.2, 32.09, 33.98, 45.3, 67.96};*/

    //sizing for standard arial font
    private static final double[] availableFontSizes = new double[] { 6, 7, 7.5, 8, 9, 10, 10.5, 11, 12, 13, 13.5, 14, 14.5, 15, 16, 17, 18, 20, 22, 24, 26, 27, 28, 29, 30, 32, 34, 36, 48, 72};

    private static final double[] SC1 = new double[] { 6, 8, 12, 13 + 1.0 / 3, 14 + 2.0 / 3, 20 + 2.0 / 3, 18 + 2.0 / 3, 6, 8 + 2.0 / 3, 8 + 2.0 / 3, 10 + 2.0 / 3, 14, 8, 8, 8, 7 + 1.0 / 3};
    // SC - special characters                        ' ' !   "             #             $             %             &  '            (            )             *   +  ,  -  .            /
    //                                         ASCII: 32-                                                                                                                                -47

    private static final double digits = 14 + 2.0 / 3;
    //all digits are the same pixel width
    //                     ASCII: 48-      -57

    private static final double[] SC2 = new double[] { 8, 8, 14, 14, 14, 14, 24, 8 + 2.0 / 3, 7 + 1.0 / 3, 8 + 2.0 / 3, 14, 12, 8, 9 + 1.0 / 3, 7 + 1.0 / 3, 9 + 1.0 / 3, 14};
    // SC - special characters                         :  ;   <   =   >   ?   @            [            \            ]   ^   _  `            {            |            }   ~
    //                                         ASCII: 58-                   -64           91-                                 -96          123-                         -126

    private static final double[] UPW24 = new double[] { 17 + 1.0 / 3, 17 + 1.0 / 3, 18, 18, 16 + 2.0 / 3, 14 + 2.0 / 3, 19 + 1.0 / 3, 18 + 2.0 / 3, 8, 14, 18, 14 + 2.0 / 3, 20, 18 + 2.0 / 3, 19 + 1.0 / 3, 16 + 2.0 / 3, 19 + 1.0 / 3, 17 + 1.0 / 3, 16 + 2.0 / 3, 15 + 1.0 / 3, 18 + 2.0 / 3, 16 + 2.0 / 3, 22 + 2.0 / 3, 14 + 2.0 / 3, 15 + 1.0 / 3, 16};
    //    UPW24 - uppercasePixelWidthsAtSize24                      A             B   C   D             E             F             G             H  I   J   K             L   M             N             O             P             Q             R             S             T             U             V             W             X             Y   Z
    //                                  ASCII:                     65-                                                                                                                                                                                                                                                                                   -90

    private static final double[] LPW24 = new double[] { 14 + 2.0 / 3, 15 + 1.0 / 3, 14 + 2.0 / 3, 15 + 1.0 / 3, 14 + 2.0 / 3, 8, 15 + 1.0 / 3, 14 + 2.0 / 3, 6 + 2.0 / 3, 6 + 2.0 / 3, 14, 6 + 2.0 / 3, 21 + 1.0 / 3, 14 + 2.0 / 3, 14 + 2.0 / 3, 15 + 1.0 / 3, 15 + 1.0 / 3, 10 + 2.0 / 3, 13 + 1.0 / 3, 8 + 2.0 / 3, 14 + 2.0 / 3, 13 + 1.0 / 3, 20, 12 + 2.0 / 3, 13 + 1.0 / 3, 12 + 2.0 / 3};
    //    LPW24 - lowercasePixelWidthsAtSize24                      a             b             c             d             e  f             g             h            i            j   k            l             m             n             o             p             q             r             s            t             u             v   w             x             y             z
    //                                  ASCII:                     97-                                                                                                                                                                                                                                                                                                                      -122

    //uppercase pixel width of W at every size
    private static final double[] sizeW = new double[] { 6, 6 + 2.0 / 3, 7 + 1.0 / 3, 8, 8 + 2.0 / 3, 10, 10, 10 + 2.0 / 3, 11 + 1.0 / 3, 12 + 2.0 / 3, 12 + 2.0 / 3, 13 + 1.0 / 3, 14, 14 + 2.0 / 3, 15 + 1.0 / 3, 16, 17 + 1.0 / 3, 19 + 1.0 / 3, 20 + 2.0 / 3, 22 + 2.0 / 3, 24 + 2.0 / 3, 25 + 1.0 / 3, 26 + 2.0 / 3, 27 + 1.0 / 3, 28 + 2.0 / 3, 30 + 2.0 / 3, 32, 34, 45 + 1.0 / 3, 68};
    //                                                                6            7          7.5  8            9  10 10.5           11            12            13          13.5            14 14.5           15            16  17            18            20            22            24            26            27            28            29            30            32  34  36            48  72

    //correction term subtracted from each character in a string's calculated pixel width
    private static final double averagePixelErrorPerCharacter = 0.3724461813;


    public static double findNearestFontSize(Double initFontSize) {
        if(findFontIndex(initFontSize) != null) { return initFontSize; }

        int smallerFontIndex = availableFontSizes.length - 1;
        for (; smallerFontIndex >= 0 && availableFontSizes[smallerFontIndex] > initFontSize; smallerFontIndex--);

        int largerFontIndex = 0;
        for (; largerFontIndex < availableFontSizes.length && availableFontSizes[largerFontIndex] < initFontSize; largerFontIndex++);

        if (smallerFontIndex < 0) { return availableFontSizes[0]; }
        if (largerFontIndex > availableFontSizes.length - 1) { return availableFontSizes[availableFontSizes.length - 1]; }

        double average = (availableFontSizes[smallerFontIndex] + availableFontSizes[largerFontIndex]) / 2.0;

        if (initFontSize < average) { return availableFontSizes[smallerFontIndex]; }

        return availableFontSizes[largerFontIndex];
    }

    public static int findPixelWidth(String text, Double fontSize, int length) {
        Integer fontIndex = findFontIndex(fontSize);

        if (fontIndex == null) { throw new InvalidParameterException("Font not found"); }

        double count = 0;

        for (int i = 0; i < length; i++) {
            Double width = findWidthAt24(text.charAt(i));

            count += interpretFontSize(text.charAt(i), fontIndex, width);
        }

        //correct the pixel error
        count = count - averagePixelErrorPerCharacter * text.length();

        return (int) count + 1;
    }

    private static Integer findFontIndex(Double fontSize) {
        int fontIndex = 0;

        for (; fontIndex < availableFontSizes.length; fontIndex++) {
            if (availableFontSizes[fontIndex] == fontSize) { return fontIndex; }
        }
        //for (; fontIndex < availableFontSizes.length && availableFontSizes[fontIndex] != fontSize; fontIndex++);

        if (fontIndex == availableFontSizes.length) { return null; }

        return fontIndex;
    }

    private static Double findWidthAt24(char c) {
        if (c < 32 || c > 126) { return 15.2; }

        if (c <= 47) { return SC1[c - 32]; }
        if (c <= 57) { return digits; }
        if (c <= 64) { return SC2[c - 58]; }
        if (c <= 90) { return UPW24[c - 65]; }
        if (c <= 96) { return SC2[c - 84]; }
        if (c <= 122) { return LPW24[c - 97]; }
        return SC2[c - 110];
    }

    private static Double interpretFontSize(char c, Integer fontIndex, Double width) {
        if (availableFontSizes[fontIndex] == 24) { return width; }

        double percentageSizeOfCapitalW = width / sizeW[19];

        return percentageSizeOfCapitalW * sizeW[fontIndex]; //this is the equation: (c@24 / W@24) = (c@fontSize / W@fontSize)
        //                                                           simplified to: c@fontSize = (c@24 / W@24) * W@fontSize
    }
}
