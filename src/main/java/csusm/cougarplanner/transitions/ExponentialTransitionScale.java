package csusm.cougarplanner.transitions;

import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Custom JavaFX {@link Transition} that animates a node's Y-scale along a
 * decelerating parabolic curve (fast at the start, gently easing into the
 * end value). Used for expand/collapse animations where the linear default
 * feels too mechanical.
 * <p>
 * The curve is the parabola <code>y = -(end - start) * (fraction - 1)^2 + end</code>,
 * which passes through {@code (0, start)} and {@code (1, end)} and has zero
 * slope at {@code fraction = 1} — giving the soft landing at the end.
 *
 * @see ExponentialTransitionTranslation for the translate-based counterpart
 */
public class ExponentialTransitionScale extends Transition {
    private final Node node;
    private final double startValue;
    private final double endValue;
    
    public ExponentialTransitionScale(Node node, double startValue, double endValue, Duration duration) {
        this.node = node;
        this.startValue = startValue;
        this.endValue = endValue;
        setCycleDuration(duration);
    }

    @Override
    protected void interpolate(double fraction) {
        //parabolic exponential growth/decay function - y = - (end - start) * (x - 1)^(2) + end
        double value = (-1 * (endValue - startValue) * Math.pow((fraction - 1),2)) + endValue;

        node.setScaleY(value);
    }
}
