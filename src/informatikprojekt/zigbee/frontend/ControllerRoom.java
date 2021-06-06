package informatikprojekt.zigbee.frontend;

import informatikprojekt.zigbee.Main;
import informatikprojekt.zigbee.util.LineGraph;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerRoom implements Initializable {
    public ColorPicker guiColorPicker;
    public TextField guiBSize;
    public AnchorPane drawingArea;
    private Line currentLine;
    private LineGraph lineGraph = new LineGraph();
    private Circle currentCircle = null;

    enum TOOL_TYPE {
        NONE, WAND, DEVICE, DELETE
    }


    private TOOL_TYPE activeTool = TOOL_TYPE.NONE;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void guiWand(ActionEvent actionEvent) {

        if (activeTool != TOOL_TYPE.WAND) {
            activeTool = TOOL_TYPE.WAND;
            Image image = new Image("informatikprojekt/zigbee/frontend/cursor/pen.png");
            Main.s.setCursor(new ImageCursor(image, (image.getWidth() / 2) - 512, (image.getHeight() / 2) + 512));
        } else {
            activeTool = TOOL_TYPE.NONE;
            Main.s.setCursor(Cursor.DEFAULT);
        }
    }

    public void guiDelete(ActionEvent actionEvent) {
    }

    public void guiDevice(ActionEvent actionEvent) {
    }

    public void guiClearAll(ActionEvent actionEvent) {

        drawingArea.getChildren().clear();
        lineGraph = new LineGraph();

    }

    public void onDragDetected(MouseEvent mouseEvent) {
        if (activeTool == TOOL_TYPE.WAND) {
            drawingArea.startFullDrag();
            drawingArea.getChildren().forEach(Node::startFullDrag);
            mouseEvent.consume();
        }
    }

    public void onMouseDragEntered(MouseDragEvent mouseDragEvent) {
        if (activeTool == TOOL_TYPE.WAND) {

            if (lineGraph.isEmpty()) {
                currentCircle = new Circle();
                currentLine = new Line();
                currentCircle.setRadius(15);
                currentCircle.setFill(Color.TRANSPARENT);
                currentCircle.setStroke(Color.BLACK);
                currentCircle.setStrokeWidth(4);
                currentCircle.setCenterX(mouseDragEvent.getX());
                currentCircle.setCenterY(mouseDragEvent.getY());
                currentLine.setStrokeWidth(Double.parseDouble(guiBSize.getText()));
                currentLine.setStroke(guiColorPicker.getValue());
                currentLine.setStartX(mouseDragEvent.getX());
                currentLine.setStartY(mouseDragEvent.getY());

                drawingArea.getChildren().add(currentCircle);
                drawingArea.getChildren().add(currentLine);
                lineGraph.addCircle(currentCircle);
                setupCircleHandlers(currentCircle);

            }
        }
    }

    public void onMouseDragExited(MouseDragEvent mouseDragEvent) {
    }

    public void onMouseDragReleased(MouseDragEvent mouseDragEvent) {

        if (activeTool == TOOL_TYPE.WAND && currentLine != null) {

            List<Circle> matches = new LinkedList<>();

            drawingArea.getChildren().forEach(e ->
            {
                if (e instanceof Circle && e.contains(mouseDragEvent.getX(), mouseDragEvent.getY()))
                    matches.add((Circle) e);
            });

            Circle circle;

            if (matches.isEmpty()) {
                currentLine.setEndX(mouseDragEvent.getX());
                currentLine.setEndY(mouseDragEvent.getY());
                circle = new Circle();
                circle.setRadius(15);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(4);
                circle.setCenterX(mouseDragEvent.getX());
                circle.setCenterY(mouseDragEvent.getY());
                drawingArea.getChildren().add(circle);
                setupCircleHandlers(circle);
                lineGraph.addCircle(circle);
            } else {
                circle = matches.get(matches.size() - 1);
                currentLine.setEndX(circle.getCenterX());
                currentLine.setEndY(circle.getCenterY());
            }

            lineGraph.addEdge(circle, currentCircle);
            currentCircle = null;
            currentLine = null;

        }
        mouseDragEvent.consume();
    }

    public void onMouseDragOver(MouseDragEvent mouseDragEvent) {

        if (activeTool == TOOL_TYPE.WAND && currentLine != null) {


            List<Circle> matches = new LinkedList<>();

            drawingArea.getChildren().forEach(e ->
            {
                if (e instanceof Circle && e.contains(mouseDragEvent.getX(), mouseDragEvent.getY()))
                    matches.add((Circle) e);
            });

            if (matches.isEmpty()) {

                if(mouseDragEvent.getX() < 20){

                    currentLine.setEndX(20);
                    currentLine.setEndY(mouseDragEvent.getY());

                }else if(mouseDragEvent.getY() < 20){
                    currentLine.setEndX(mouseDragEvent.getX());
                    currentLine.setEndY(20);

                }else {
                    currentLine.setEndX(mouseDragEvent.getX());
                    currentLine.setEndY(mouseDragEvent.getY());
                }
            } else {
                Circle c = matches.get(matches.size() - 1);
                currentLine.setEndX(c.getCenterX());
                currentLine.setEndY(c.getCenterY());
            }

        }
    }

    private void setupCircleHandlers(Circle c) {
        c.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            c.startFullDrag();
            event.consume();
        });
        c.addEventFilter(MouseDragEvent.MOUSE_DRAG_ENTERED, event ->
        {
            if (activeTool == TOOL_TYPE.WAND && currentLine == null) {
                currentLine = new Line();
                currentLine.setStrokeWidth(Double.parseDouble(guiBSize.getText()));
                currentLine.setStroke(guiColorPicker.getValue());
                Circle source = (Circle) event.getSource();
                currentLine.setStartX(source.getCenterX());
                currentLine.setStartY(source.getCenterY());
                drawingArea.getChildren().add(currentLine);

                currentCircle = source;
            }

        });
    }
}