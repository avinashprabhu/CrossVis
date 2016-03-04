package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.histogram.Histogram;
import gov.ornl.csed.cda.histogram.HistogramPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by csg on 2/3/16.
 */
public class MultiViewPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(MultiViewPanel.class);

    private int plotHeight;
    private int plotUnitWidth = 1;
    private Box panelBox;
    private Font fontAwesomeFont = null;

    private boolean linkPanelScrollBars = false;
    private Color dataColor = new Color(80, 80, 130, 180);
    private ArrayList<ViewInfo> viewInfoList = new ArrayList<ViewInfo>();
    private HashMap<String, GroupInfo> viewGroupMap = new HashMap<>();

    private boolean syncGroupScrollbars = false;
    private boolean showOverview = true;
    private boolean showButtonPanels = true;
    private ChronoUnit detailChronoUnit = ChronoUnit.SECONDS;
    private TimeSeriesPanel.PlotDisplayOption timeSeriesDisplayOption = TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE;

    public MultiViewPanel (int plotHeight) {
        this.plotHeight = plotHeight;

        InputStream is = FalconFX.class.getResourceAsStream("fontawesome-webfont.ttf");
        try {
            fontAwesomeFont = Font.createFont(Font.TRUETYPE_FONT, is);
            fontAwesomeFont = fontAwesomeFont.deriveFont(Font.PLAIN, 12F);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialize();
    }

    public void setSyncGroupScollbarsEnabled (boolean enabled) {
        if (syncGroupScrollbars != enabled) {
            syncGroupScrollbars = enabled;
            for (GroupInfo groupInfo : viewGroupMap.values()) {
                groupInfo.syncScrollBars = enabled;
                if (syncGroupScrollbars) {
                    // if enabling the sync, set all scrollbars to the position of the first item in the group
                    // TODO: determine which view is highest in the list and set others based on that view's scrollbar position
                    if (groupInfo.viewInfoList.size() > 1) {
                        ViewInfo firstView = groupInfo.viewInfoList.get(0);
                        for (int i = 1; i < groupInfo.viewInfoList.size(); i++) {
                            ViewInfo viewInfo = groupInfo.viewInfoList.get(i);
                            viewInfo.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().getModel().setValue(firstView.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().getModel().getValue());
                        }
                    }
                }
            }
        }
    }

    public boolean getShowButtonPanelsEnabled() {
        return showButtonPanels;
    }

    public void setShowButtonPanelsEnabled (boolean enabled) {
        if (showButtonPanels != enabled) {
            showButtonPanels = enabled;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.buttonPanel.setVisible(enabled);
            }
            panelBox.repaint();
        }
    }

    public boolean getSyncGroupScrollbarsEnabled() {
        return syncGroupScrollbars;
    }

    public boolean getShowOverviewEnabled() {
        return showOverview;
    }

//    public boolean getAlignTimeSeriesEnabled () {
//        return alignTimeSeries;
//    }

    public ChronoUnit getDetailChronoUnit() {
        return detailChronoUnit;
    }

    public void setDetailChronoUnit(ChronoUnit chronoUnit) {
        if (detailChronoUnit != chronoUnit) {
            detailChronoUnit = chronoUnit;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.detailTimeSeriesPanel.setPlotChronoUnit(detailChronoUnit);
            }
        }
    }

    public void setDataColor(Color color) {
        if (dataColor != color) {
            dataColor = color;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.detailTimeSeriesPanel.setDataColor(dataColor);
                viewInfo.overviewTimeSeriesPanel.setDataColor(dataColor);
            }
        }
    }

    public int getChronoUnitWidth() {
        return plotUnitWidth;
    }

    public void setChronoUnitWidth (int plotUnitWidth) {
        if (this.plotUnitWidth != plotUnitWidth) {
            this.plotUnitWidth = plotUnitWidth;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.detailTimeSeriesPanel.setPlotUnitWidth(plotUnitWidth);
            }
        }
    }

    public Color getDataColor() {
        return dataColor;
    }


//    public void setAlignTimeSeriesEnabled (boolean enabled) {
//        if (alignTimeSeries != enabled) {
//            if (enabled) {
//                // enabling aligned timeseries
//                for (ViewInfo viewInfo : viewInfoList) {
//                    viewInfo.detailTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
//                    viewInfo.overviewTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
//                }
//            } else {
//                // disabling aligned timeseries
//                for (ViewInfo viewInfo : viewInfoList) {
//                    viewInfo.detailTimeSeriesPanel.setDisplayTimeRange(viewInfo.timeSeries.getStartInstant(), viewInfo.timeSeries.getEndInstant());
//                    viewInfo.overviewTimeSeriesPanel.setDisplayTimeRange(viewInfo.timeSeries.getStartInstant(), viewInfo.timeSeries.getEndInstant());
//                }
//            }
//
//            alignTimeSeries = enabled;
//        }
//    }

    public int getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(int plotHeight) {
        this.plotHeight = plotHeight;
        for (ViewInfo viewInfo : viewInfoList) {
            viewInfo.viewPanel.setPreferredSize(new Dimension(viewInfo.viewPanel.getPreferredSize().width, plotHeight));
            viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
            viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        }
        revalidate();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBackground(Color.white);

        panelBox = new Box(BoxLayout.PAGE_AXIS);
        JScrollPane scroller = new JScrollPane(panelBox);
        add(scroller, BorderLayout.CENTER);
    }

    private void rebuildBoxPanel() {
        panelBox.removeAll();
        for (ViewInfo viewInfo : viewInfoList) {
            panelBox.add(viewInfo.viewPanel);
        }
        revalidate();
        panelBox.repaint();
    }

    private JPanel createButtonPanel (ViewInfo viewInfo) {
        JButton moveUpButton = new JButton();
        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = viewInfoList.indexOf(viewInfo);
                if (index != 0) {
                    viewInfoList.set(index, viewInfoList.get(index - 1));
                    viewInfoList.set(index - 1, viewInfo);
                    rebuildBoxPanel();
                }
            }
        });

        JButton moveDownButton = new JButton();
        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = viewInfoList.indexOf(viewInfo);
                if (index != (viewInfoList.size() - 1)) {
                    viewInfoList.set(index, viewInfoList.get(index + 1));
                    viewInfoList.set(index+1, viewInfo);
                    rebuildBoxPanel();
                }
            }
        });

        JButton removeButton = new JButton();
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (viewInfoList.remove(viewInfo)) {
                    // TODO: Need to remove from group here and reset time range for the group
                    rebuildBoxPanel();
                }
            }
        });

        JButton settingsButton = new JButton();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
//        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.setLayout(new GridLayout(4, 0, 0, 0));
        buttonPanel.add(moveUpButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(moveDownButton);

        if (fontAwesomeFont != null) {
            buttonPanel.setPreferredSize(new Dimension(40, 100));
            moveDownButton.setFont(fontAwesomeFont);
            moveDownButton.setText("\uf078");
            moveUpButton.setFont(fontAwesomeFont);
            moveUpButton.setText("\uf077");
            removeButton.setFont(fontAwesomeFont);
            removeButton.setText("\uf1f8");
            settingsButton.setFont(fontAwesomeFont);
            settingsButton.setText("\uf085");
        } else {
            moveUpButton.setText("Move Up");
            moveDownButton.setText("Move Down");
            removeButton.setText("Remove");
            settingsButton.setText("Settings");
        }

        return buttonPanel;
    }

    public void setShowOverviewEnabled(boolean enabled) {
        if (enabled != showOverview) {
            showOverview = enabled;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.sidePanel.setVisible(showOverview);
            }
        }
    }

    public void addTimeSeries(TimeSeries timeSeries, String groupName) {
        ViewInfo viewInfo = new ViewInfo();
        viewInfo.timeSeries = timeSeries;
        viewInfoList.add(viewInfo);

        // assign view to a group (create new one if necessary)
        GroupInfo groupInfo = viewGroupMap.get(groupName);
        if (groupInfo == null) {
            groupInfo = new GroupInfo();
            groupInfo.name = groupName;
            groupInfo.startInstant = timeSeries.getStartInstant();
            groupInfo.endInstant = timeSeries.getEndInstant();
            groupInfo.syncScrollBars = syncGroupScrollbars;
            viewGroupMap.put(groupName, groupInfo);
        } else {
            boolean resetOtherTimeRanges = false;
            if (timeSeries.getStartInstant().isBefore(groupInfo.startInstant)) {
                groupInfo.startInstant = timeSeries.getStartInstant();
                resetOtherTimeRanges = true;
            }
            if (timeSeries.getEndInstant().isAfter(groupInfo.endInstant)) {
                groupInfo.endInstant = timeSeries.getEndInstant();
                resetOtherTimeRanges = true;
            }

            // if using common time scale, reset all times series panels to use overall start and end for group
            if (resetOtherTimeRanges && groupInfo.useCommonTimeScale) {
                for (ViewInfo view : groupInfo.viewInfoList) {
                    view.detailTimeSeriesPanel.setDisplayTimeRange(groupInfo.startInstant, groupInfo.endInstant);
                    view.overviewTimeSeriesPanel.setDisplayTimeRange(groupInfo.startInstant, groupInfo.endInstant);
                }
            }
        }
        groupInfo.viewInfoList.add(viewInfo);

        viewInfo.viewPanel = new JPanel();
        viewInfo.viewPanel.setPreferredSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        viewInfo.viewPanel.setBackground(Color.white);
        viewInfo.viewPanel.setLayout(new BorderLayout());

        viewInfo.buttonPanel = createButtonPanel(viewInfo);

        viewInfo.detailTimeSeriesPanel = new TimeSeriesPanel(1, detailChronoUnit, timeSeriesDisplayOption);
        if (groupInfo.useCommonTimeScale) {
            viewInfo.detailTimeSeriesPanel.setTimeSeries(timeSeries, groupInfo.startInstant, groupInfo.endInstant);
        } else {
            viewInfo.detailTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }

        viewInfo.detailsTimeSeriesPanelScrollPane = new JScrollPane(viewInfo.detailTimeSeriesPanel);
        viewInfo.detailsTimeSeriesPanelScrollPane.setPreferredSize(new Dimension(100, 100));
        viewInfo.detailsTimeSeriesPanelScrollPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        viewInfo.detailHistogramPanel = new HistogramPanel(HistogramPanel.ORIENTATION.VERTICAL, HistogramPanel.STATISTICS_MODE.MEAN_BASED);
        viewInfo.detailHistogramPanel.setBackground(Color.white);
        viewInfo.detailHistogramPanel.setPreferredSize(new Dimension(50, 80));

        viewInfo.overviewTimeSeriesPanel = new TimeSeriesPanel(1, timeSeriesDisplayOption);
        viewInfo.overviewTimeSeriesPanel.setShowTimeRangeLabels(false);
        viewInfo.overviewTimeSeriesPanel.setBackground(Color.white);
        if (groupInfo.useCommonTimeScale) {
            viewInfo.overviewTimeSeriesPanel.setTimeSeries(timeSeries, groupInfo.startInstant, groupInfo.endInstant);
        } else {
            viewInfo.overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }

        viewInfo.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                JScrollBar scrollBar = (JScrollBar)e.getSource();
                double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
                double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
//                double deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
//                Instant startHighlightInstant = timeSeries.getStartInstant().plusMillis((long)deltaTime);
//                int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
//                norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
//                deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
//                Instant endHighlightInstant = timeSeries.getEndInstant().minusMillis((long) deltaTime);
//                viewInfo.overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);

//                ArrayList<TimeSeriesRecord> recordList = timeSeries.getRecordsBetween(startHighlightInstant, endHighlightInstant);
//                if (recordList != null && !recordList.isEmpty()) {
//                    double values[] = new double[recordList.size()];
//                    for (int i = 0; i < recordList.size(); i++) {
//                        double value = recordList.get(i).value;
//                        if (!Double.isNaN(value)) {
//                            values[i] = value;
//                        }
//                    }
//                    Histogram detailHistogram = new Histogram(timeSeries.getName(), values, viewInfo.overviewHistogramPanel.getBinCount());
//                    viewInfo.detailHistogramPanel.setHistogram(detailHistogram);
//                    viewInfo.overviewHistogramPanel.setHighlightValues(values);
//                }

                //TODO: Need to make a map to quickly get the group for a timeview;  This needs to be cleaned up
                // if the view is in a group with more than one view and the group is set to sync scrolling, sync those suckers here
                for (GroupInfo group : viewGroupMap.values()) {
                    if (group.viewInfoList.contains(viewInfo)) {
                        if ((group.viewInfoList.size() > 1) && group.syncScrollBars) {
                            // go through the list of views and set all scroll bars to the value of this view's scroll bar
                            for (ViewInfo view : group.viewInfoList) {
                                // don't set scroll position of this scroll bar
                                if ((view != viewInfo) && (view.detailsTimeSeriesPanelScrollPane != null)) {
                                    view.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().getModel().setValue(scrollBar.getModel().getValue());
                                }
                            }
                        }

                        // set highlight range based on the group start / end time range
                        double deltaTime = norm * Duration.between(group.startInstant, group.endInstant).toMillis();
                        Instant startHighlightInstant = group.startInstant.plusMillis((long)deltaTime);
                        int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                        norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                        deltaTime = norm * Duration.between(group.startInstant, group.endInstant).toMillis();
                        Instant endHighlightInstant = group.endInstant.minusMillis((long) deltaTime);
                        viewInfo.overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);

                        ArrayList<TimeSeriesRecord> recordList = timeSeries.getRecordsBetween(startHighlightInstant, endHighlightInstant);
                        if (recordList != null && !recordList.isEmpty()) {
                            double values[] = new double[recordList.size()];
                            for (int i = 0; i < recordList.size(); i++) {
                                double value = recordList.get(i).value;
                                if (!Double.isNaN(value)) {
                                    values[i] = value;
                                }
                            }
                            Histogram detailHistogram = new Histogram(timeSeries.getName(), values, viewInfo.overviewHistogramPanel.getBinCount());
                            viewInfo.detailHistogramPanel.setHistogram(detailHistogram);
                            viewInfo.overviewHistogramPanel.setHighlightValues(values);
                        }

                        break;
                    }
                }
            }
        });

        viewInfo.overviewHistogramPanel = new HistogramPanel(HistogramPanel.ORIENTATION.HORIZONTAL, HistogramPanel.STATISTICS_MODE.MEAN_BASED);

        ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
        double values[] = new double[records.size()];
        for (int i = 0; i < values.length; i++) {
            double value = records.get(i).value;
            if (!Double.isNaN(value)) {
                values[i] = value;
            }
        }

        Histogram histogram = new Histogram(timeSeries.getName(), values, viewInfo.overviewHistogramPanel.getBinCount());
        viewInfo.overviewHistogramPanel.setBackground(Color.white);
        viewInfo.overviewHistogramPanel.setHistogram(histogram);

        viewInfo.sidePanel = new JPanel();
        viewInfo.sidePanel.setBackground(Color.WHITE);
        viewInfo.sidePanel.setLayout(new GridLayout(2, 1));
        viewInfo.sidePanel.add(viewInfo.overviewHistogramPanel);
        viewInfo.sidePanel.add(viewInfo.overviewTimeSeriesPanel);
        viewInfo.sidePanel.setPreferredSize(new Dimension(200, plotHeight));
        viewInfo.sidePanel.setBorder(BorderFactory.createTitledBorder("Overview"));
        viewInfo.sidePanel.setVisible(showOverview);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BorderLayout());
        detailsPanel.add(viewInfo.detailsTimeSeriesPanelScrollPane, BorderLayout.CENTER);
        detailsPanel.add(viewInfo.detailHistogramPanel, BorderLayout.EAST);

        viewInfo.viewPanel.add(detailsPanel, BorderLayout.CENTER);
        viewInfo.viewPanel.add(viewInfo.sidePanel, BorderLayout.EAST);
        viewInfo.viewPanel.add(viewInfo.buttonPanel, BorderLayout.WEST);
        viewInfo.viewPanel.setBorder(BorderFactory.createTitledBorder(timeSeries.getName()));

//        if (groupInfo.syncScrollBars && (groupInfo.viewInfoList.size() > 1)) {
//            ViewInfo firstView = groupInfo.viewInfoList.get(0);
//            log.debug("first view scrollbar value is " + firstView.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().getModel().getValue());
//            viewInfo.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().getModel().setValue(firstView.detailsTimeSeriesPanelScrollPane.getHorizontalScrollBar().getModel().getValue());
//        }

        panelBox.add(viewInfo.viewPanel);
        revalidate();
    }

    public void drawToImage(File imageFile) throws IOException {
        int imageWidth = getWidth() * 4;
        int imageHeight = getHeight() * 4;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setTransform(AffineTransform.getScaleInstance(4., 4.));

        paint(g2);
        g2.dispose();

        ImageIO.write(image, "png", imageFile);
    }

    public static void main (String args[]) throws IOException {
        int numTimeSeries = 6;
        int numTimeSeriesRecords = 60*48;
        double minValue = -10.;
        double maxValue = 10.;
        double valueIncrement = (maxValue - minValue) / numTimeSeriesRecords;
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//        Instant endInstant = Instant.from(startInstant).plus(numTimeSeriesRecords + 50, ChronoUnit.MINUTES);

        ArrayList<TimeSeries> timeSeriesList = new ArrayList<>();
        for (int i = 0; i < numTimeSeries; i++) {
            double value = 0.;
            double uncertaintyValue = 0.;
            TimeSeries timeSeries = new TimeSeries("V"+i);
            for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
                value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
//                value = minValue + (itime * valueIncrement);
                timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
            }
            timeSeriesList.add(timeSeries);

            startInstant = startInstant.plus(60, ChronoUnit.MINUTES);
        }

//        TimeSeriesPanel overviewPanel = new TimeSeriesPanel(1, TimeSeriesPanel.PlotDisplayOption.LINE);
//        overviewPanel.setTimeSeries(timeSeriesList.get(0));
        MultiViewPanel multiViewPanel = new MultiViewPanel(150);
//        multiViewPanel.setAlignTimeSeriesEnabled(true);
        JScrollPane scroller = new JScrollPane(multiViewPanel);


//        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        frame.setSize(1000, 300);
        frame.setVisible(true);

        for (int i = 0; i < 3; i++) {
            TimeSeries timeSeries = timeSeriesList.get(i);
            multiViewPanel.addTimeSeries(timeSeries, "Group 1");
        }

        for (int i = 3; i < numTimeSeries; i++) {
            TimeSeries timeSeries = timeSeriesList.get(i);
            multiViewPanel.addTimeSeries(timeSeries, "Group 2");
        }



//        multiViewPanel.setShowOverviewEnabled(false);
//        multiViewPanel.setShowOverviewEnabled(true);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        multiViewPanel.setShowButtonPanelsEnabled(false);

//        log.debug("Saving screen capture");
//        File imageFile = new File("test.png");
//        multiViewPanel.drawToImage(imageFile);
    }

    private class ViewInfo {
        public TimeSeries timeSeries;
        public JPanel viewPanel;
        public JPanel sidePanel;
        public JPanel buttonPanel;
        public JScrollPane detailsTimeSeriesPanelScrollPane;
        public TimeSeriesPanel detailTimeSeriesPanel;
        public TimeSeriesPanel overviewTimeSeriesPanel;
        public HistogramPanel overviewHistogramPanel;
        public HistogramPanel detailHistogramPanel;
    }

    private class GroupInfo{
        public String name;
        public Instant startInstant;
        public Instant endInstant;
        public boolean useCommonTimeScale = true;
        public boolean syncScrollBars;
        public ArrayList<ViewInfo> viewInfoList = new ArrayList<>();
    }
}