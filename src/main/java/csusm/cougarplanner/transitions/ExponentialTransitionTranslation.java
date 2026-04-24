package csusm.cougarplanner.transitions;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Custom JavaFX {@link Transition} that animates a node's translation along
 * a decelerating parabolic curve — fast at the start, easing softly into
 * the end value. Used for panel slide-in / slide-out effects.
 * <p>
 * The {@code isHorizontal} flag selects whether the animation drives
 * {@link Node#setTranslateX} (true) or {@link Node#setTranslateY} (false),
 * so one class covers both sliding directions.
 * <p>
 * The curve is the parabola <code>y = -(end - start) * (fraction - 1)^2 + end</code>,
 * which passes through {@code (0, start)} and {@code (1, end)} with zero
 * slope at the end to produce the soft landing.
 */
public class ExponentialTransitionTranslation extends Transition {
    private final Node node;
    private final double startValue;
    private final double endValue;
    private final boolean isHorizontal; //identifies the direction of the translation, vertical or horizontal

    public ExponentialTransitionTranslation(Node node, boolean isHorizontal, double startValue, double endValue, Duration duration) {
        this.node = node;
        this.startValue = startValue;
        this.endValue = endValue;
        setCycleDuration(duration);
        this.isHorizontal = isHorizontal;
    }

    @Override
    protected void interpolate(double fraction) {
        //parabolic exponential growth/decay function - y = - (end - start) * (x - 1)^(2) + end
        double value = (-1 * (endValue - startValue) * Math.pow((fraction - 1),2)) + endValue;

        if (isHorizontal) {
            node.setTranslateX(value);
        } else {
            node.setTranslateY(value);
        }
    }
}
