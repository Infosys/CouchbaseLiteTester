/*
 * Copyright (c) 2020.  amrishraje@gmail.com
 */

package CBLiteTester;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class SwitchButton extends StackPane {
    private final Rectangle back = new Rectangle(30, 10, Color.RED);
    private final Button toggle = new Button();
    private String buttonStyleOff = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 0.2, 0.0, 0.0, 2); -fx-background-color: WHITE;";
    private String buttonStyleOn = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 0.2, 0.0, 0.0, 2); -fx-background-color: #00893d;";

    public boolean isContinuous() {
        return state;
    }

    private boolean state;

    private void init() {
        getChildren().addAll(back, toggle);
        setMinSize(30, 15);
        back.maxWidth(30);
        back.minWidth(30);
        back.maxHeight(10);
        back.minHeight(10);
        back.setArcHeight(back.getHeight());
        back.setArcWidth(back.getHeight());
        back.setFill(Color.valueOf("#ced5da"));
        toggle.setShape(new Circle(2));
        setAlignment(toggle, Pos.CENTER_LEFT);
        toggle.setMaxSize(15, 15);
        toggle.setMinSize(15, 15);
        toggle.setStyle(buttonStyleOff);
    }

    public SwitchButton() {
        init();
        EventHandler<Event> click = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (state) {
                    toggle.setStyle(buttonStyleOff);
                    back.setFill(Color.valueOf("#ced5da"));
                    setAlignment(toggle, Pos.CENTER_LEFT);
                    state = false;
                } else {
                    toggle.setStyle(buttonStyleOn);
                    back.setFill(Color.valueOf("#80C49E"));
                    setAlignment(toggle, Pos.CENTER_RIGHT);
                    state = true;
                }
            }
        };

        setOnMouseClicked(click);
        toggle.setOnMouseClicked(click);
        back.setOnMouseClicked(click);
        toggle.setMouseTransparent(true);
    }
}