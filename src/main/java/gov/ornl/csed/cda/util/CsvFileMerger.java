package gov.ornl.csed.cda.util;/*
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


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class CsvFileMerger {

    private static String usage = "mess up";

    public static void main(String[] args) {

        File file1 = null;
        File file2 = null;
        File outputFile = null;

        Integer appenderKeyCol;
        Integer appendeeKeyCol;

        // √ check to see if the appropriate number of CLA
        if (args.length != 5) {
            System.out.println(args.length);

        } else {

            // - read in CLA

            // appendee
            file1 = new File(args[0]);

            // appender
            file2 = new File(args[1]);
            outputFile = new File(args[2]);

            appenderKeyCol = Integer.parseInt(args[4]) - 1;
            appendeeKeyCol = Integer.parseInt(args[3]) - 1;

            ArrayList<CSVRecord> appendeeEntries = new ArrayList<>();
            TreeMap<Double, CSVRecord> appenderEntries = new TreeMap<>();


            // - read in the two csv files

//            BufferedWriter csvWriter = new BufferedWriter(new FileWriter(dstCSVFile));

            CSVParser file1Parser = null;
            CSVParser file2Parser = null;

            try {
                file1Parser = new CSVParser(new FileReader(file1), CSVFormat.DEFAULT);
                file2Parser = new CSVParser(new FileReader(file2), CSVFormat.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            CSVRecord file1HeaderRecord = file1Parser.iterator().next();
            CSVRecord file2HeaderRecord = file2Parser.iterator().next();

//            StringBuffer buffer = new StringBuffer();

            for (CSVRecord record : file1Parser) {
                appendeeEntries.add(record);
            }

            System.out.println();
            System.out.println();

            for (CSVRecord record : file2Parser) {

                appenderEntries.put(Double.valueOf(record.get(appenderKeyCol)), record);
            }

            BufferedWriter csvWriter = null;
            try {
                csvWriter = new BufferedWriter(new FileWriter(outputFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuffer buffer = new StringBuffer();

            // - write out the headers
            for (int j = 0; j < file1HeaderRecord.size(); j++) {
                buffer.append(file1HeaderRecord.get(j) + ",");

            }
            buffer.deleteCharAt(buffer.length() - 1);

            for (int j = 0; j < file2HeaderRecord.size(); j++) {
                if (j == appenderKeyCol) {
                    continue;
                }

                buffer.append(file2HeaderRecord.get(j) + ",");

            }
            buffer.deleteCharAt(buffer.length() - 1);

            try {
                csvWriter.write(buffer.toString().trim() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }


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
                buffer.deleteCharAt(buffer.length() - 1);

                for (int j = 0; j < appender.size(); j++) {
                    if (j == appenderKeyCol) {
                        continue;
                    }
                    buffer.append(appender.get(j) + ",");

                }
                buffer.deleteCharAt(buffer.length() - 1);

                // - write out the new table to a new csv file
                try {
                    csvWriter.write(buffer.toString().trim() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return;
    }
}
