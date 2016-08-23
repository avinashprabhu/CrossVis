package gov.ornl.csed.cda.util;

/*
 *
 *  Class:  [CLASS NAME]
 *
 *      Author:     whw
 *
 *      Created:    10 Aug 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  [PARENT CLASS]
 *
 *  Interfaces:     [INTERFACES USED]
 *
 */



/*
WHAT I'M GOING TO DO

    √ check to see if the appropriate number of CLA
    - read in CLA
    - read in the two csv files
    ? input about which columns (one in each file) to key off of
    - for each line in the appendee file
        - check the value of the key column
        - pull the corresponding row from the appender file
        √ make sure that multiple feature vectors do NOT exist (then we would have to choose and which one?) tree map should handle this
        - concatenate the appender features to the appendee vector
     - write out the new table to a new csv file

 */

import javafx.application.Application;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.TreeMap;

public class CsvFileMerger extends Application {

    // the necessaries
    private static String usage = "read the documentation";
    private final static Logger log = LoggerFactory.getLogger(CsvFileMerger.class);

    // the other
    File file1 = null;
    File file2 = null;
    File outputFile = null;
    Integer file1KeyCol = null;
    Integer file2KeyCol = null;

    public static void merge(File appendeeFile, File appenderFile, File outputFile, Integer appendeeKeyCol, Integer appenderKeyCol) throws IOException {
        ArrayList<CSVRecord> appendeeEntries = new ArrayList<>();
        TreeMap<Double, CSVRecord> appenderEntries = new TreeMap<>();


        // - read in the two csv files
        CSVParser file1Parser = new CSVParser(new FileReader(appendeeFile), CSVFormat.DEFAULT);
        CSVParser file2Parser = new CSVParser(new FileReader(appenderFile), CSVFormat.DEFAULT);

        CSVRecord file1HeaderRecord = file1Parser.iterator().next();
        CSVRecord file2HeaderRecord = file2Parser.iterator().next();

//        System.out.println("checkpoint1");

        for (CSVRecord record : file1Parser) {
            appendeeEntries.add(record);
        }

//        System.out.println("checkpoint2");

        for (CSVRecord record : file2Parser) {

            appenderEntries.put(Double.valueOf(record.get(appenderKeyCol)), record);
        }

//        System.out.println("checkpoint3");

        BufferedWriter csvWriter = null;
        csvWriter = new BufferedWriter(new FileWriter(outputFile));

        StringBuffer buffer = new StringBuffer();

        // - write out the headers
        for (int j = 0; j < file1HeaderRecord.size(); j++) {
            buffer.append(file1HeaderRecord.get(j) + ",");

        }

//        System.out.println("checkpoint4");

        for (int j = 0; j < file2HeaderRecord.size(); j++) {
            if (j == appenderKeyCol) {
                continue;
            }

            buffer.append(file2HeaderRecord.get(j) + ",");

        }

//        System.out.println("checkpoint5");

        buffer.deleteCharAt(buffer.length() - 1);

        csvWriter.write(buffer.toString().trim() + "\n");

        // - for each line in the appendee file
        for (int i = 0; i < appendeeEntries.size(); i++) {
            CSVRecord temp = appendeeEntries.get(i);
            buffer = new StringBuffer();

            // - check the value of the key column
            Double appendeeKeyValue = Double.valueOf(temp.get(appendeeKeyCol));

            // - pull the corresponding row from the appender file
            CSVRecord appender = appenderEntries.get(appendeeKeyValue);

            // - concatenate the appender features to the appendee vector
            for (int j = 0; j < temp.size(); j++) {
                buffer.append(temp.get(j) + ",");

            }

            for (int j = 0; j < appender.size(); j++) {
                if (j == appenderKeyCol) {
                    continue;
                }
                buffer.append(appender.get(j) + ",");

            }

//            System.out.println("checkpoint6");

            buffer.deleteCharAt(buffer.length() - 1);

            // - write out the new table to a new csv file
            csvWriter.write(buffer.toString().trim() + "\n");

        }

//        System.out.println("checkpoint7");

        file1Parser.close();
        file2Parser.close();
        csvWriter.flush();
        csvWriter.close();

//        System.out.println("before1");
        return;
    }


    public static void main(String[] args) throws IOException {

        File file1 = null;
        File file2 = null;
        File outputFile = null;

        Integer appenderKeyCol;
        Integer appendeeKeyCol;

        // √ check to see if the appropriate number of CLA for comman line or not
        if (args.length != 5) {
            // launch the GUI
//            System.out.println(args.length);
            launch(args);

        } else {
            // run from commandLine

            // - read in CLA

            System.out.println("Running Command Line Version");
            // appendee
            file1 = new File(args[0]);

            // appender
            file2 = new File(args[1]);

            outputFile = new File(args[2]);

            appenderKeyCol = Integer.parseInt(args[4]) - 1;
            appendeeKeyCol = Integer.parseInt(args[3]) - 1;

            CsvFileMerger.merge(file1, file2, outputFile, appendeeKeyCol, appenderKeyCol);

        }
//        System.out.println("before2");
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        GridPane root = new GridPane();

        root.setAlignment(Pos.CENTER);
        root.setHgap(100);
        root.setVgap(100);
//        root.setGridLinesVisible(true);
        root.setPrefSize(Screen.getPrimary().getBounds().getWidth() * 0.5, Screen.getPrimary().getBounds().getHeight() * 0.5);

        ChoiceBox<String> cb1 = new ChoiceBox<>();
        ChoiceBox<String> cb2 = new ChoiceBox<>();

        Button button = new Button("Go");

        button.setOnAction(e -> {
            try {
                System.out.println("BUTTON PRESS");
                String choice1 = cb1.getValue();
                ObservableList<String> list1 = cb1.getItems();

                String choice2 = cb2.getValue();
                ObservableList<String> list2 = cb2.getItems();

                if (true) {
                    CsvFileMerger.merge(file1, file2, outputFile, list1.indexOf(choice1), list2.indexOf(choice2));
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            primaryStage.close();
            System.exit(0);
        });

        root.add(cb1, 1, 1);
        root.add(cb2, 2, 1);
        root.add(button, 2, 2);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        primaryStage.setOnShown(e -> {

            // choose the three files
            FileChooser filechooser1 = new FileChooser();
            filechooser1.setTitle("Choose the Appendee CSV File");
            file1 = filechooser1.showOpenDialog(primaryStage);
//            log.debug(String.valueOf(file1));

            FileChooser fileChooser2 = new FileChooser();
            fileChooser2.setTitle("Choose the Appender CSV File");
            file2 = fileChooser2.showOpenDialog(primaryStage);
//            log.debug(String.valueOf(file2));

            FileChooser outputFileChooser = new FileChooser();
            outputFileChooser.setTitle("Choose the Output File");
            outputFile = outputFileChooser.showSaveDialog(primaryStage);

            // - open the files and read in the column names
            CSVParser file1Parser = null;
            CSVParser file2Parser = null;

            try {
                file1Parser = new CSVParser(new FileReader(file1), CSVFormat.DEFAULT);
                file2Parser = new CSVParser(new FileReader(file2), CSVFormat.DEFAULT);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            CSVRecord file1HeaderRecord = null;
            CSVRecord file2HeaderRecord = null;

            if (file1Parser != null && file2Parser != null) {
                file1HeaderRecord = file1Parser.iterator().next();
                file2HeaderRecord = file2Parser.iterator().next();
            }

            // - populate the choice boxes
            for (int i = 0; file1HeaderRecord != null && i < file1HeaderRecord.size(); i++) {
                cb1.getItems().add(i, file1HeaderRecord.get(i));
            }

            for (int i = 0; file2HeaderRecord != null && i < file2HeaderRecord.size(); i++) {
                cb2.getItems().add(i, file2HeaderRecord.get(i));
            }

            try {
                file1Parser.close();
                file2Parser.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        });

        primaryStage.show();

    }
}
