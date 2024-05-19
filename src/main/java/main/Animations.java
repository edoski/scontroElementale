package main;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class Animations {

	//    ANIMATION UTILITY METHODS
	public static FadeTransition fadeIn(Node node, double changeByValue, double seconds, Interpolator interpolator) {
		FadeTransition fade = new FadeTransition(Duration.seconds(seconds), node);
		fade.setByValue(changeByValue);
		fade.setInterpolator(interpolator);
		return fade;
	}

	public static ScaleTransition scale(Node node, double changeX, double changeY, double seconds, Interpolator interpolator) {
		ScaleTransition scale = new ScaleTransition(Duration.seconds(seconds), node);
		scale.setByX(changeX);
		scale.setByY(changeY);
		scale.setInterpolator(interpolator);
		return scale;
	}

	public static TranslateTransition translateY(Node node, double changeY, double seconds, Interpolator interpolator) {
		TranslateTransition translateY = new TranslateTransition(Duration.seconds(seconds), node);
		translateY.setByY(changeY);
		translateY.setInterpolator(interpolator);
		return translateY;
	}
}