package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class UnivariateAxis extends Axis {
    public final static double DEFAULT_BAR_WIDTH = 20d;
    public final static double DEFAULT_NARROW_BAR_WIDTH = 10d;
    public final static double HOVER_TEXT_SIZE = 8d;
    public final static double RANGE_VALUE_TEXT_HEIGHT = 14;
//    public final static Color DEFAULT_TEXT_FILL = Color.BLACK;
//    public final static double DEFAULT_TEXT_SIZE = 8d;

    protected Text hoverValueText;
    private double contextRegionHeight = 20;

    private Rectangle upperContextBar;
    private Rectangle lowerContextBar;
    private Line upperContextBarHandle;
    private Line lowerContextBarHandle;

    private Rectangle axisBar;

    public UnivariateAxis(DataTableView dataTableView, Column column) {
        super(dataTableView, column);

        axisBar = new Rectangle();
        axisBar.setStroke(DEFAULT_AXIS_BAR_STROKE_COLOR);
        axisBar.setFill(DEFAULT_AXIS_BAR_FILL_COLOR);
//        axisBar.setFill(null);
        axisBar.setWidth(DEFAULT_BAR_WIDTH);
        axisBar.setSmooth(true);
        axisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        upperContextBar = new Rectangle();
        upperContextBar.setStroke(DEFAULT_AXIS_BAR_CONTEXT_STROKE_COLOR);
        upperContextBar.setFill(DEFAULT_AXIS_BAR_CONTEXT_FILL_COLOR);
        upperContextBar.setSmooth(true);
        upperContextBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        upperContextBarHandle = new Line();
        upperContextBarHandle.setStrokeLineCap(StrokeLineCap.ROUND);
        upperContextBarHandle.setStroke(DEFAULT_CONTEXT_LIMIT_HANDLE_COLOR);
        upperContextBarHandle.setStrokeWidth(4.);
        upperContextBarHandle.setCursor(Cursor.V_RESIZE);

        lowerContextBar = new Rectangle();
        lowerContextBar.setStroke(axisBar.getStroke());
        lowerContextBar.setFill(upperContextBar.getFill());
        lowerContextBar.setSmooth(true);
        lowerContextBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        lowerContextBarHandle = new Line();
        lowerContextBarHandle.setStrokeLineCap(StrokeLineCap.ROUND);
        lowerContextBarHandle.setStroke(upperContextBarHandle.getStroke());
        lowerContextBarHandle.setStrokeWidth(upperContextBarHandle.getStrokeWidth());
        lowerContextBarHandle.setCursor(Cursor.V_RESIZE);

        hoverValueText = new Text();
        hoverValueText.setFont(new Font(HOVER_TEXT_SIZE));
        hoverValueText.setSmooth(true);
        hoverValueText.setVisible(false);
        hoverValueText.setFill(DEFAULT_TEXT_COLOR);
        hoverValueText.setTextOrigin(VPos.BOTTOM);
        hoverValueText.setMouseTransparent(true);
        getDataTableView().getPane().getChildren().add(hoverValueText);

        if (getGraphicsGroup().getChildren().contains(axisSelectionGraphicsGroup)) {
            getGraphicsGroup().getChildren().remove(axisSelectionGraphicsGroup);
            getGraphicsGroup().getChildren().addAll(upperContextBar, lowerContextBar, axisBar, axisSelectionGraphicsGroup,
                    upperContextBarHandle, lowerContextBarHandle);
        }

        registerListeners();
    }

    private void registerListeners() { }

    protected abstract Object getValueForAxisPosition(double axisPosition);

    public Rectangle getAxisBar() { return axisBar; }

    public double getBarLeftX() { return axisBar.getX(); }

    public double getBarRightX() { return axisBar.getX() + axisBar.getWidth(); }

    public Line getUpperContextBarHandle() { return upperContextBarHandle; }

    public Line getLowerContextBarHandle() { return lowerContextBarHandle; }

    public Rectangle getUpperContextBar() { return upperContextBar; }

    public Rectangle getLowerContextBar() { return lowerContextBar; }

    public double getMinFocusPosition() { return axisBar.getY() + axisBar.getHeight(); }

    public double getMaxFocusPosition() { return axisBar.getY(); }

    @Override
    public void resize (double left, double top, double width, double height) {
        super.resize(left, top, width, height);

        double barTop = getTitleText().getLayoutBounds().getMaxY() + RANGE_VALUE_TEXT_HEIGHT + 4;
        double barBottom = getBounds().getMaxY() - RANGE_VALUE_TEXT_HEIGHT;
        double focusTop = barTop + contextRegionHeight;
        double focusBottom = barBottom - contextRegionHeight;

        axisBar.setX(getCenterX() - (axisBar.getWidth() / 2.));
        axisBar.setY(focusTop);
        axisBar.setHeight(focusBottom - focusTop);

        maxHistogramBinWidth = (getBounds().getWidth() - axisBar.getWidth()) / 2.;
        
        upperContextBar.setX(axisBar.getX());
        upperContextBar.setWidth(axisBar.getWidth());
        upperContextBar.setY(barTop);
        upperContextBar.setHeight(contextRegionHeight);

        lowerContextBar.setX(axisBar.getX());
        lowerContextBar.setWidth(axisBar.getWidth());
        lowerContextBar.setY(focusBottom);
        lowerContextBar.setHeight(contextRegionHeight);

        upperContextBarHandle.setStartX(getAxisBar().getX() - 5.);
        upperContextBarHandle.setEndX(getAxisBar().getX() + getAxisBar().getWidth() + 5.);
        upperContextBarHandle.setStartY(getAxisBar().getY() - 2.);
        upperContextBarHandle.setEndY(upperContextBarHandle.getStartY());

        lowerContextBarHandle.setStartX(upperContextBarHandle.getStartX());
        lowerContextBarHandle.setEndX(upperContextBarHandle.getEndX());
        lowerContextBarHandle.setStartY(getAxisBar().getY() + getAxisBar().getHeight() + 2.);
        lowerContextBarHandle.setEndY(lowerContextBarHandle.getStartY());

        if (!getAxisSelectionList().isEmpty()) {
            for (AxisSelection axisSelection : getAxisSelectionList()) {
                axisSelection.resize();
            }
        }
    }

    protected Line makeLine() {
        Line line = new Line();
        line.setStroke(Color.DARKGRAY);
        line.setSmooth(true);
        line.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        return line;
    }
}
