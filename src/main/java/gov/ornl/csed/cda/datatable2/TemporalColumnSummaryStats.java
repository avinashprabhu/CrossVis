package gov.ornl.csed.cda.datatable2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;

public class TemporalColumnSummaryStats extends ColumnSummaryStats {
    private static final int DEFAULT_NUM_HISTOGRAM_BINS = 50;
    private static final int MAX_NUM_HISTOGRAM_BINS = 100;

    private SimpleObjectProperty<Instant> startInstant;
    private SimpleObjectProperty<Instant> endInstant;
    private SimpleObjectProperty<TemporalHistogram> histogram;

    private Instant[] values;

    private int numHistogramBins = DEFAULT_NUM_HISTOGRAM_BINS;

    public TemporalColumnSummaryStats(Column column) {
        super(column);
    }

    public TemporalColumnSummaryStats(Column column, Instant startInstant, Instant endInstant) {
        super(column);

        setStartInstant(startInstant);
        setEndInstant(endInstant);
    }

    public void setValues(Instant[] values, int numHistogramBins) {
        this.values = values;
        this.numHistogramBins = numHistogramBins;
        calculateStatistics();
    }

    public void setValues(Instant[] values) {
        this.values = values;

        numHistogramBins = (int)Math.floor(Math.sqrt(values.length));
        if (numHistogramBins > MAX_NUM_HISTOGRAM_BINS) {
            numHistogramBins = MAX_NUM_HISTOGRAM_BINS;
        }

        calculateStatistics();
    }

    public void setNumHistogramBins(int numBins) {
        if (numBins != numHistogramBins) {
            numHistogramBins = numBins;
            calculateHistogram();
        }
    }

    public int getNumHistogramBins() {
        return numHistogramBins;
    }

    public Instant[] getValues() {
        return values;
    }

    @Override
    public void calculateStatistics() {
        int columnIndex = getColumn().getDataModel().getColumnIndex(column);
        for (int i = 0; i < values.length; i++) {
//        for (int i = 0; i < getColumn().getDataModel().getTupleCount(); i++) {
            Instant instant = values[i];
//            Instant instant = (Instant)getColumn().getDataModel().getTuple(0).getElement(columnIndex);
            if (i == 0) {
                setStartInstant(instant);
                setEndInstant(instant);
            } else {
                if (instant.isBefore(getStartInstant())) {
                    setStartInstant(instant);
                } else if (instant.isAfter(getEndInstant())) {
                    setEndInstant(instant);
                }
            }
        }

        calculateHistogram();
    }

    @Override
    public void calculateHistogram() {
        setHistogram(new TemporalHistogram(column.getName(), values, numHistogramBins, getStartInstant(), getEndInstant()));
    }

    public TemporalHistogram getHistogram() {
        return histogramProperty().get();
    }

    public void setHistogram(TemporalHistogram histogram) {
        histogramProperty().set(histogram);
    }

    public ObjectProperty<TemporalHistogram> histogramProperty() {
        if (histogram == null) {
            histogram = new SimpleObjectProperty<>(this, "histogram");
        }
        return histogram;
    }

//    @Override
//    public void calculateQueryStatistics() {
//        int columnIndex = getColumn().getDataModel().getColumnIndex(column);
//
//        int counter = 0;
//        for (Tuple tuple : getColumn().getDataModel().getActiveQuery().getQueriedTuples()) {
//            Instant instant = (Instant)tuple.getElement(columnIndex);
//            if (counter == 0) {
//                setStartInstant(instant);
//                setEndInstant(instant);
//            } else {
//                if (instant.isBefore(getStartInstant())) {
//                    setStartInstant(instant);
//                } else if (instant.isAfter(getEndInstant())) {
//                    setEndInstant(instant);
//                }
//            }
//            counter++;
//        }
//    }


    public void setStartInstant(Instant startInstant) {
        startInstantProperty().set(startInstant);
//        startLocalDateTimeProperty().set(LocalDateTime.ofInstant(startInstant, ZoneOffset.UTC));
    }

    public Instant getStartInstant() {
        return startInstantProperty().get();
    }

    public ObjectProperty<Instant> startInstantProperty() {
        if (startInstant == null) {
            startInstant = new SimpleObjectProperty<>(this, "startInstant");
        }
        return startInstant;
    }
//
//    public SimpleObjectProperty<LocalDateTime> startLocalDateTimeProperty() {
//        if (startLocalDateTime == null) {
//            startLocalDateTime = new SimpleObjectProperty<>(this, "startLocalDateTime");
//        }
//        return startLocalDateTime;
//    }

    public void setEndInstant(Instant endInstant) {
        endInstantProperty().set(endInstant);
//        endLocalDateTimeProperty().set(LocalDateTime.ofInstant(endInstant, ZoneOffset.UTC));
    }

    public Instant getEndInstant() {
        return endInstantProperty().get();
    }

    public ObjectProperty<Instant> endInstantProperty() {
        if (endInstant == null) {
            endInstant = new SimpleObjectProperty<>(this, "endInstant");
        }
        return endInstant;
    }
}