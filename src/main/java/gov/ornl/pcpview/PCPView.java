package gov.ornl.pcpview;

import gov.ornl.datatable.*;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class PCPView extends Region implements DataModelListener {
    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;
    public final static Color DEFAULT_OVERALL_SUMMARY_FILL_COLOR = new Color(Color.LIGHTSTEELBLUE.getRed(), Color.LIGHTSTEELBLUE.getGreen(), Color.LIGHTSTEELBLUE.getBlue(), 0.6d);
    public final static Color DEFAULT_QUERY_SUMMARY_FILL_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.6d);
    public final static Color DEFAULT_OVERALL_SUMMARY_STROKE_COLOR = Color.DARKGRAY.darker();
    public final static Color DEFAULT_QUERY_SUMMARY_STROKE_COLOR = Color.BLACK;
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private final static double DEFAULT_LINE_OPACITY = 0.5;
    private final static Color DEFAULT_SELECTED_ITEMS_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_LINE_OPACITY);
    private final static Color DEFAULT_UNSELECTED_ITEMS_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_LINE_OPACITY);
    private final static DISPLAY_MODE DEFAULT_DISPLAY_MODE = DISPLAY_MODE.SUMMARY;

    private final Logger log = Logger.getLogger(PCPView.class.getName());

    private TupleDrawingAnimationTimer selectedTuplesTimer;
    private TupleDrawingAnimationTimer unselectedTuplesTimer;
    private DoubleProperty drawingProgressProperty;
    private Canvas selectedCanvas;
    private Canvas unselectedCanvas;
    private ObjectProperty<Color> selectedItemsColor;
    private ObjectProperty<Color> unselectedItemsColor;
    private ObjectProperty<Color> overallSummaryFillColor;
    private ObjectProperty<Color> querySummaryFillColor;
    private ObjectProperty<Color> overallSummaryStrokeColor;
    private ObjectProperty<Color> querySummaryStrokeColor;
    private ObjectProperty<Color> backgroundColor;
    private ObjectProperty<Color> labelsColor;
    private BooleanProperty showSelectedItems;
    private BooleanProperty showUnselectedItems;
    private Pane pane;
    private double axisSpacing = 40d;
    private DataModel dataModel;
    private ArrayList<PCPAxis> axisList;
    private ArrayList<PCPTuple> tupleList;
    private HashSet<PCPTuple> unselectedTupleSet;
    private HashSet<PCPTuple> selectedTupleSet;
    private ObjectProperty<DISPLAY_MODE> displayMode;
    private boolean fitAxisSpacingToWidthEnabled = true;
    private DoubleProperty nameTextRotation;
    private ArrayList<PCPBinSet> PCPBinSetList;

    public PCPView() {
        initialize();
        registerListeners();
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resizeView());
        heightProperty().addListener(o -> resizeView());

        backgroundColor.addListener((observable, oldValue, newValue) -> {
            pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        });

        labelsColor.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
//                for (PCPAxis pcpAxis : axisList) {
//                    pcpAxis.setLabelColor(newValue);
//                }
            }
        });

        selectedItemsColor.addListener((observable, oldValue, newValue) -> {
            redrawView();
        });

        unselectedItemsColor.addListener((observable, oldValue, newValue) -> {
            redrawView();
        });

        showSelectedItems.addListener(((observable, oldValue, newValue) -> {
            redrawView();
        }));

        showUnselectedItems.addListener(((observable, oldValue, newValue) -> {
            redrawView();
        }));

        displayMode.addListener(((observable, oldValue, newValue) -> {
            if ((oldValue == DISPLAY_MODE.PCP_LINES) || (oldValue == DISPLAY_MODE.PCP_BINS) || (oldValue == DISPLAY_MODE.SUMMARY)) {
//                lineGC.clearRect(0, 0, getWidth(), getHeight());
                selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
                unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());

                if (oldValue == DISPLAY_MODE.PCP_LINES) {
                    if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
                        selectedTuplesTimer.stop();
                    }
                    if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
                        unselectedTuplesTimer.stop();
                    }
                }
            }

            if (newValue == DISPLAY_MODE.PCP_LINES) {
                fillTupleSets();
            }

            resizeView();
        }));
    }

    private void fillTupleSets() {
        unselectedTupleSet.clear();
        selectedTupleSet.clear();
        if ((tupleList != null) && (!tupleList.isEmpty())) {
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                for (PCPTuple pcpTuple : tupleList) {
                    if (pcpTuple.getTuple().getQueryFlag()) {
                        selectedTupleSet.add(pcpTuple);
                    } else {
                        unselectedTupleSet.add(pcpTuple);
                    }
                }
            } else {
                selectedTupleSet.addAll(tupleList);
            }
        }
    }

    private void initialize() {
        axisList = new ArrayList<>();
        unselectedTupleSet = new HashSet<>();
        selectedTupleSet = new HashSet<>();
        nameTextRotation = new SimpleDoubleProperty(0.0);
        selectedItemsColor = new SimpleObjectProperty<>(DEFAULT_SELECTED_ITEMS_COLOR);
        unselectedItemsColor = new SimpleObjectProperty<>(DEFAULT_UNSELECTED_ITEMS_COLOR);

        overallSummaryFillColor = new SimpleObjectProperty<>(DEFAULT_OVERALL_SUMMARY_FILL_COLOR);
        overallSummaryStrokeColor = new SimpleObjectProperty<>(DEFAULT_OVERALL_SUMMARY_STROKE_COLOR);
        querySummaryFillColor = new SimpleObjectProperty<>(DEFAULT_QUERY_SUMMARY_FILL_COLOR);
        querySummaryStrokeColor = new SimpleObjectProperty<>(DEFAULT_QUERY_SUMMARY_STROKE_COLOR);

        showSelectedItems = new SimpleBooleanProperty(true);
        showUnselectedItems = new SimpleBooleanProperty(true);
        displayMode = new SimpleObjectProperty<>(DEFAULT_DISPLAY_MODE);

        drawingProgressProperty = new SimpleDoubleProperty(0d);

        selectedCanvas = new Canvas(getWidth(), getHeight());
        unselectedCanvas = new Canvas(getWidth(), getHeight());

        labelsColor = new SimpleObjectProperty<>(DEFAULT_LABEL_COLOR);
        backgroundColor = new SimpleObjectProperty<>(DEFAULT_BACKGROUND_COLOR);

        pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        pane.getChildren().addAll(unselectedCanvas, selectedCanvas);

        getChildren().add(pane);
    }

    public int getAxisSpacing () {return (int)axisSpacing;}

    public void setAxisSpacing(double axisSpacing) {
        this.axisSpacing = axisSpacing;
        resizeView();
    }

    public final PCPAxis getAxis(int index) {
        return axisList.get(index);
    }

    public final int getAxisCount() { return axisList.size(); }

    private void drawTuplePolylines() {
        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        selectedCanvas.getGraphicsContext2D().setLineDashes(null);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        unselectedCanvas.getGraphicsContext2D().setLineDashes(null);

        if ((isShowingUnselectedItems()) && (unselectedTupleSet != null) && (!unselectedTupleSet.isEmpty())) {
            if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
                unselectedTuplesTimer.stop();
            }

            unselectedTuplesTimer = new TupleDrawingAnimationTimer(unselectedCanvas, unselectedTupleSet, axisList, getUnselectedItemsColor(), 100);
            unselectedTuplesTimer.start();
        }

        if ((isShowingSelectedItems()) && (selectedTupleSet != null) && (!selectedTupleSet.isEmpty())) {
            if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
                selectedTuplesTimer.stop();
            }

            selectedTuplesTimer = new TupleDrawingAnimationTimer(selectedCanvas, selectedTupleSet, axisList, getSelectedItemsColor(), 100);
            selectedTuplesTimer.start();
        }
    }

    private void redrawView() {
        if (getDisplayMode() == DISPLAY_MODE.PCP_LINES) {
            if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
                selectedTuplesTimer.stop();
            }

            if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
                unselectedTuplesTimer.stop();
            }

            drawTuplePolylines();
        } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
            drawPCPBins();
        } else if (getDisplayMode() == DISPLAY_MODE.SUMMARY) {
            drawSummaryShapes();
        }
    }

    private void drawSummaryShapes() {
//        lineCanvas.setCache(false);
//        lineGC.setLineCap(StrokeLineCap.BUTT);
//        lineGC.clearRect(0, 0, getWidth(), getHeight());
//        lineGC.setLineWidth(2);
//        lineGC.setLineWidth(2d);
        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        selectedCanvas.getGraphicsContext2D().setLineDashes(2d, 2d);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        unselectedCanvas.getGraphicsContext2D().setLineDashes(2d, 2d);

        for (int iaxis = 1; iaxis < axisList.size(); iaxis++) {
            PCPAxis axis1 = axisList.get(iaxis);
            PCPAxis axis0 = axisList.get(iaxis-1);

            if (axis0 instanceof PCPDoubleAxis && axis1 instanceof PCPDoubleAxis) {
                PCPDoubleAxis dAxis0 = (PCPDoubleAxis)axis0;
                PCPDoubleAxis dAxis1 = (PCPDoubleAxis)axis1;
                
                if (!(Double.isNaN(dAxis0.getOverallTypicalLine().getEndY())) &&
                        !(Double.isNaN(dAxis1.getOverallTypicalLine().getStartY()))) {

                    double xValues[] = new double[]{dAxis0.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                            dAxis1.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                            dAxis1.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                            dAxis0.getOverallDispersionRectangle().getLayoutBounds().getMaxX()};
                    double yValues[] = new double[]{dAxis0.getOverallDispersionRectangle().getLayoutBounds().getMaxY(),
                            dAxis1.getOverallDispersionRectangle().getLayoutBounds().getMaxY(),
                            dAxis1.getOverallDispersionRectangle().getLayoutBounds().getMinY(),
                            dAxis0.getOverallDispersionRectangle().getLayoutBounds().getMinY()};

                    unselectedCanvas.getGraphicsContext2D().setFill(getOverallSummaryFillColor());

                    unselectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);

                    unselectedCanvas.getGraphicsContext2D().setStroke(getOverallSummaryStrokeColor());
                    unselectedCanvas.getGraphicsContext2D().strokeLine(dAxis0.getBarRightX(), dAxis0.getOverallTypicalLine().getEndY(),
                            dAxis1.getBarLeftX(), dAxis1.getOverallTypicalLine().getStartY());
                }

                if (dataModel.getActiveQuery().hasColumnSelections()) {
                    if (!(Double.isNaN(dAxis0.getQueryTypicalLine().getEndY())) &&
                            !(Double.isNaN(dAxis1.getQueryTypicalLine().getStartY()))) {

                        double xValues[] = new double[]{dAxis0.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                                dAxis1.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                                dAxis1.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                                dAxis0.getOverallDispersionRectangle().getLayoutBounds().getMaxX()};
                        double yValues[] = new double[]{dAxis0.getQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                                dAxis1.getQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                                dAxis1.getQueryDispersionRectangle().getLayoutBounds().getMinY(),
                                dAxis0.getQueryDispersionRectangle().getLayoutBounds().getMinY()};

                        selectedCanvas.getGraphicsContext2D().setFill(getQuerySummaryFillColor());
                        selectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
                        //                selectedCanvas.getGraphicsContext2D().setStroke(getQuerySummaryStrokeColor());
                        //                selectedCanvas.getGraphicsContext2D().strokePolyline(xValues, yValues, xValues.length);

                        selectedCanvas.getGraphicsContext2D().setStroke(getQuerySummaryStrokeColor());
                        selectedCanvas.getGraphicsContext2D().strokeLine(dAxis0.getBarRightX(), dAxis0.getQueryTypicalLine().getEndY(),
                                dAxis1.getBarLeftX(), dAxis1.getQueryTypicalLine().getStartY());
                    }
                }
            }
        }
    }


    private void resizeView() {
        if (dataModel != null && !dataModel.isEmpty()) {
            double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());
            double pcpWidth;
            double width;

            if (fitAxisSpacingToWidthEnabled) {
                width = getWidth();
                pcpWidth = width - (getInsets().getLeft() + getInsets().getRight());
                axisSpacing = pcpWidth / dataModel.getColumnCount();
            } else {
                pcpWidth = axisSpacing * dataModel.getColumnCount();
                width = (getInsets().getLeft() + getInsets().getRight()) + pcpWidth;
            }

            if (pcpWidth > 0 && pcpHeight > 0) {
                pane.setPrefSize(width, getHeight());
                pane.setMinWidth(width);

                selectedCanvas.setWidth(width);
                selectedCanvas.setHeight(getHeight());
                unselectedCanvas.setWidth(width);
                unselectedCanvas.setHeight(getHeight());

                double left = getInsets().getLeft() + (axisSpacing / 2.);
                double top = getInsets().getTop();

                if (axisList != null) {
                    for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                        PCPAxis pcpAxis = axisList.get(iaxis);
                        pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);

                        if (getDisplayMode() == DISPLAY_MODE.HISTOGRAM) {
                            pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                            pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
                        }
                    }

                    // add tuples polylines from data model
                    if (getDisplayMode() == DISPLAY_MODE.PCP_LINES) {
                        if (tupleList != null) {
                            for (PCPTuple pcpTuple : tupleList) {
                                pcpTuple.layout(axisList);
                            }
                        }
                    } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
                        // layout PCPBins
                        for (PCPBinSet PCPBinSet : PCPBinSetList) {
                            PCPBinSet.layoutBins();
                        }
                    }
                }

                redrawView();
            }
        }
    }

    public void clearQuery() {
        dataModel.clearActiveQuery();

        // clear all axis selection graphics
        for (PCPAxis pcpAxis : axisList) {
            if (!pcpAxis.getAxisSelectionList().isEmpty()) {
                for (PCPAxisSelection pcpAxisSelection : pcpAxis.getAxisSelectionList()) {
                    pane.getChildren().remove(pcpAxisSelection.getGraphicsGroup());
                }
                pcpAxis.getAxisSelectionList().clear();
            }
        }

        handleQueryChange();
    }

    public double getPCPVerticalBarHeight() {
        if (axisList != null && !axisList.isEmpty()) {
            return axisList.get(0).getAxisBar().getHeight();
        }
        return Double.NaN;
    }

    private void drawPCPBins() {
//        lineCanvas.setCache(false);
//        lineGC.setLineCap(StrokeLineCap.BUTT);
//        lineGC.clearRect(0, 0, getWidth(), getHeight());
////        lineGC.setGlobalAlpha(lineOpacity);
//        lineGC.setLineWidth(2);
//        lineGC.setLineWidth(2d);

        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(2d);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(2d);

        if ((PCPBinSetList != null) && (!PCPBinSetList.isEmpty())) {
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                if (isShowingUnselectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            Color binColor = new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                                    getUnselectedItemsColor().getBlue(), bin.fillColor.getOpacity());
//                            lineGC.setFill(binColor);
                            unselectedCanvas.getGraphicsContext2D().setFill(binColor);

                            double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                            double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};

                            unselectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
//                            lineGC.fillPolygon(xValues, yValues, xValues.length);
                        }
                    }
                }

                if (isShowingSelectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            if (bin.queryCount > 0) {
                                //                        lineGC.setFill(bin.queryFillColor);
                                Color binColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                                        getSelectedItemsColor().getBlue(), bin.queryFillColor.getOpacity());
//                                lineGC.setFill(binColor);

                                selectedCanvas.getGraphicsContext2D().setFill(binColor);

                                double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                                double yValues[] = new double[]{bin.leftQueryTop, bin.rightQueryTop, bin.rightQueryBottom, bin.leftQueryBottom};
//                                lineGC.fillPolygon(xValues, yValues, xValues.length);

                                selectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
                            }
                        }
                    }
                }
            } else {
                if (isShowingSelectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            Color binColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                                    getSelectedItemsColor().getBlue(), bin.fillColor.getOpacity());
//                            lineGC.setFill(binColor);

                            selectedCanvas.getGraphicsContext2D().setFill(binColor);

                            double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                            double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};
//                            lineGC.fillPolygon(xValues, yValues, xValues.length);

                            selectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
                        }
                    }
                }
            }
        }

//        lineCanvas.setCache(true);
//        lineCanvas.setCacheHint(CacheHint.QUALITY);
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
        dataModel.addDataModelListener(this);
        resizeView();
    }

    public boolean getFitAxisSpacingToWidthEnabled() { return fitAxisSpacingToWidthEnabled; }

    public void setFitAxisSpacingToWidthEnabled (boolean enabled) {
        fitAxisSpacingToWidthEnabled = enabled;
        resizeView();
    }

    public double getNameTextRotation() {
        return nameTextRotation.get();
    }

    public void setNameTextRotation(double rotation) {
        nameTextRotation.set(rotation);
    }

    public DoubleProperty nameTextRotationProperty() {
        return nameTextRotation;
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public void setBackgroundColor(Color newBackgroundColor) {
        this.backgroundColor.set(newBackgroundColor);
        pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public Color getLabelsColor() {
        return labelsColor.get();
    }

    public void setLabelsColor(Color labelsColor) {
        this.labelsColor.set(labelsColor);
    }

    public ObjectProperty<Color> labelsColorProperty() {
        return labelsColor;
    }

    public ObjectProperty<DISPLAY_MODE> displayModeProperty() { return displayMode; }

    public final DISPLAY_MODE getDisplayMode() { return displayMode.get(); }

    public final void setDisplayMode(DISPLAY_MODE newDisplayMode) { displayMode.set(newDisplayMode); }

    public final boolean isShowingSelectedItems() { return showSelectedItems.get(); }

    public final void setShowSelectedItems(boolean enabled) { showSelectedItems.set(enabled); }

    public BooleanProperty showSelectedItemsProperty() { return showSelectedItems; }

    public final boolean isShowingUnselectedItems() { return showUnselectedItems.get(); }

    public final void setShowUnselectedItems(boolean enabled) { showUnselectedItems.set(enabled); }

    public BooleanProperty showUnselectedItemsProperty() { return showUnselectedItems; }

    public final Color getSelectedItemsColor() { return selectedItemsColor.get(); }

    public final void setSelectedItemsColor(Color color) {
        selectedItemsColor.set(color);
    }

    public ObjectProperty<Color> selectedItemsColorProperty() { return selectedItemsColor; }

    public final Color getUnselectedItemsColor() { return unselectedItemsColor.get(); }

    public final void setUnselectedItemsColor(Color color) {
        unselectedItemsColor.set(color);
    }

    public ObjectProperty<Color> unselectedItemsColorProperty() { return unselectedItemsColor; }

    public final Color getOverallSummaryFillColor () { return overallSummaryFillColor.get(); }

    public final void setOverallSummaryFillColor (Color color) { overallSummaryFillColor.set(color); }

    public ObjectProperty<Color> overallSummaryFillColorProperty() { return overallSummaryFillColor; }

    public final Color getQuerySummaryFillColor () { return querySummaryFillColor.get(); }

    public final void setQuerySummaryFillColor (Color color) { querySummaryFillColor.set(color); }

    public ObjectProperty<Color> overallQueryFillColorProperty() { return querySummaryFillColor; }

    public final Color getQuerySummaryStrokeColor () { return querySummaryStrokeColor.get(); }

    public final void setQuerySummaryStrokeColor (Color color) { querySummaryStrokeColor.set(color); }

    public ObjectProperty<Color> querySummaryStrokeColor () { return querySummaryStrokeColor; }

    public final Color getOverallSummaryStrokeColor () { return overallSummaryStrokeColor.get(); }

    public final void setOverallSummaryStrokeColor (Color color) { overallSummaryStrokeColor.set(color); }

    public ObjectProperty<Color> overallSummaryStrokeColor () { return overallSummaryStrokeColor; }

    private void reinitializeLayout() {
        if (axisList.isEmpty()) {
            for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {

                PCPAxis pcpAxis;
                if (dataModel.getColumn(iaxis) instanceof TemporalColumn) {
                    pcpAxis = new PCPTemporalAxis(this, dataModel.getColumn(iaxis), dataModel, pane);
                } else {
                    pcpAxis = new PCPDoubleAxis(this, dataModel.getColumn(iaxis), dataModel, pane);
                }
                pcpAxis.nameTextRotationProperty().bind(nameTextRotationProperty());
                pane.getChildren().add(pcpAxis.getGraphicsGroup());
                axisList.add(pcpAxis);
            }
        } else {
            ArrayList<PCPAxis> newAxisList = new ArrayList<>();
            for (int iDstCol = 0; iDstCol < dataModel.getColumnCount(); iDstCol++) {
                Column column = dataModel.getColumn(iDstCol);
                for (int iSrcCol = 0; iSrcCol < axisList.size(); iSrcCol++) {
                    PCPAxis pcpAxis = axisList.get(iSrcCol);
                    if (pcpAxis.getColumn() == column) {
                        newAxisList.add(pcpAxis);
                        break;
                    }
                }
            }

            axisList = newAxisList;
        }

        // add tuples polylines from data model
        tupleList = new ArrayList<>();
        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
            Tuple tuple = dataModel.getTuple(iTuple);
            PCPTuple pcpTuple = new PCPTuple(tuple);
            tupleList.add(pcpTuple);
        }

        fillTupleSets();

        // create PCPBinSets for axis configuration
        PCPBinSetList = new ArrayList<>();
        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
            PCPBinSetList.add(binSet);
        }

        resizeView();
    }

    private void addAxis(Column column) {
        PCPAxis pcpAxis = null;
        if (column instanceof DoubleColumn) {
            pcpAxis = new PCPDoubleAxis(this, column, dataModel, pane);
        } else if (column instanceof TemporalColumn) {
            pcpAxis = new PCPTemporalAxis(this, column, dataModel, pane);
        }

        pcpAxis.nameTextRotationProperty().bind(nameTextRotationProperty());
        pane.getChildren().add(pcpAxis.getGraphicsGroup());
        axisList.add(pcpAxis);
    }

    private void handleQueryChange() {
        double left = getInsets().getLeft() + (axisSpacing / 2.);
        double top = getInsets().getTop();
        double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            PCPAxis pcpAxis = axisList.get(iaxis);
            pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);

            if (getDisplayMode() == DISPLAY_MODE.HISTOGRAM) {
                pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
            }
        }

        if (getDisplayMode() == DISPLAY_MODE.PCP_LINES) {
            fillTupleSets();
            redrawView();
        } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
            for (PCPBinSet PCPBinSet : PCPBinSetList) {
                PCPBinSet.layoutBins();
            }
            redrawView();
        } else if (getDisplayMode() == DISPLAY_MODE.SUMMARY) {
            redrawView();
        }
    }

    @Override
    public void dataModelReset(DataModel dataModel) {
        if (axisList != null && !axisList.isEmpty()) {
            for (PCPAxis pcpAxis : axisList) {
                pane.getChildren().removeAll(pcpAxis.getGraphicsGroup(), pcpAxis.getHistogramBinRectangleGroup(),
                        pcpAxis.getQueryHistogramBinRectangleGroup());
            }
            axisList.clear();
        }
        reinitializeLayout();
    }

    @Override
    public void dataModelNumHistogramBinsChanged(DataModel dataModel) {
        if (getDisplayMode() == DISPLAY_MODE.HISTOGRAM) {
            double left = getInsets().getLeft() + (axisSpacing / 2.);
            double top = getInsets().getTop();
            double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

            for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                PCPAxis pcpAxis = axisList.get(iaxis);
                pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);

                if (getDisplayMode() == DISPLAY_MODE.HISTOGRAM) {
                    pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                    pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
                }
            }
        } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
            for (PCPBinSet PCPBinSet : PCPBinSetList) {
                PCPBinSet.layoutBins();
            }
            redrawView();
        }
    }

    @Override
    public void dataModelQueryCleared(DataModel dataModel) {
        handleQueryChange();
    }

    @Override
    public void dataModelQueryColumnCleared(DataModel dataModel, Column column) {
        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionChanged(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelHighlightedColumnChanged(DataModel dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        if (dataModel.getHighlightedColumn() == null) {
            for (PCPAxis pcpAxis : axisList) {
                pcpAxis.setHighlighted(false);
            }
        } else {
            for (PCPAxis pcpAxis : axisList) {
                if (pcpAxis.getColumn() == newHighlightedColumn) {
                    pcpAxis.setHighlighted(true);
                } else if (pcpAxis.getColumn() == oldHighlightedColumn) {
                    pcpAxis.setHighlighted(false);
                }
            }
        }
    }

    @Override
    public void dataModelTuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
        reinitializeLayout();
    }

    @Override
    public void dataModelTuplesRemoved(DataModel dataModel, int numTuplesRemoved) {
        // clear
        for (PCPAxis pcpAxis : axisList) {
            for (PCPAxisSelection pcpAxisSelection : pcpAxis.getAxisSelectionList()) {
                pane.getChildren().remove(pcpAxisSelection.getGraphicsGroup());
            }
            pcpAxis.getAxisSelectionList().clear();
        }

        reinitializeLayout();
    }

    @Override
    public void dataModelColumnDisabled(DataModel dataModel, Column disabledColumn) {
        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            PCPAxis pcpAxis = axisList.get(iaxis);
            if (pcpAxis.getColumn() == disabledColumn) {
                axisList.remove(pcpAxis);

                // remove axis graphics from pane
                pane.getChildren().remove(pcpAxis.getGraphicsGroup());
                pane.getChildren().remove(pcpAxis.getHistogramBinRectangleGroup());
                pane.getChildren().remove(pcpAxis.getQueryHistogramBinRectangleGroup());

                // remove axis selection graphics
                if (!pcpAxis.getAxisSelectionList().isEmpty()) {
                    for (PCPAxisSelection axisSelection : pcpAxis.getAxisSelectionList()) {
                        pane.getChildren().remove(axisSelection.getGraphicsGroup());
                    }
                }

                // create PCPBinSets for axis configuration
                PCPBinSetList = new ArrayList<>();
                for (int i = 0; i < axisList.size()-1; i++) {
                    PCPBinSet binSet = new PCPBinSet(axisList.get(i), axisList.get(i+1), dataModel);
                    binSet.layoutBins();
                    PCPBinSetList.add(binSet);
                }

                // add tuples polylines from data model
                tupleList = new ArrayList<>();
                for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
                    Tuple tuple = dataModel.getTuple(iTuple);
                    PCPTuple pcpTuple = new PCPTuple(tuple);
                    tupleList.add(pcpTuple);
                }
                fillTupleSets();

                resizeView();
                break;
            }
        }
    }

    @Override
    public void dataModelColumnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataModelColumnEnabled(DataModel dataModel, Column enabledColumn) {
// add axis lines to the pane
        addAxis(enabledColumn);

        // add tuples polylines from data model
        tupleList = new ArrayList<>();
        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
            Tuple tuple = dataModel.getTuple(iTuple);
            PCPTuple pcpTuple = new PCPTuple(tuple);
            tupleList.add(pcpTuple);
        }

        fillTupleSets();

        // create PCPBinSets for axis configuration
        PCPBinSetList = new ArrayList<>();
        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
            PCPBinSetList.add(binSet);
        }

        resizeView();
    }

    @Override
    public void dataModelColumnOrderChanged(DataModel dataModel) {
        reinitializeLayout();
    }

    @Override
    public void dataModelColumnNameChanged(DataModel dataModel, Column column) {

    }

    public enum DISPLAY_MODE {SUMMARY, HISTOGRAM, PCP_LINES, PCP_BINS}
}