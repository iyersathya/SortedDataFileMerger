package com.merger;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    /**
     * Data class object that will hold data record.s
     */
    public static class Data {
        String data;
        long value;

        @Override
        public String toString() {
            return data;
        }

        public static Data mergeData(Data data1, Data data2) {
            if (data1 != null && data2 != null) {
                Data data = new Data();
                data.data = data1.data;
                data.value = data1.value + data2.value;
                return data;
            }
            return null;
        }
    }

    /**
     * Read files from a given folder and return list of files.
     * @param folder :folder to be read.
     * @return: list of files is returned.
     */
    private static List<String> getFilesForFolder(final File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                files.add(fileEntry.getName());
            }
        }
        return files;
    }

    public static void main(String[] args) {
        final String output_file = "output_merged.txt";
        final File folder = new File("data");

        // Step 1 : Get list of files.
        List<String> files = getFilesForFolder(folder);

        // Step 2: Create Readers for all the files.
        List<Reader> inFiles = new ArrayList();
        BufferedWriter outputFile = null;

        for (String file : files) {
            System.out.println(file);
            try {
                inFiles.add(new BufferedReader(
                        new FileReader(folder + "\\" + file))
                );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Step 3: Create Writer for output file.
        try {
            outputFile = new BufferedWriter(
                    new FileWriter(output_file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Step 4: Create a Data merget instance, passing in comparater being used in sorting of files.
        SortedDataMerger<Data> fileMerger =
                new SortedDataMerger<Data>(inFiles, outputFile, new Comparator<Data>() {
                    @Override
                    public int compare(Data o1, Data o2) {

                        if (o1 == null || o1.data == null) {
                            return -1;
                        }
                        if (o2 == null || o2.data == null) {
                            return 1;
                        }
                        if (o1.data.equals(o2.data)) {
                            return 0;
                        }
                        return o1.data.compareTo(o2.data);
                    }
                });

        // Step 4.1 Set the data Reader.
        fileMerger.setDataReader(new SortedDataMerger.DataReader<Data>() {
            @Override
            public Data readData(Reader reader) {
                try {
                    String line = ((BufferedReader) reader).readLine();
                    if (line == null) {
                        System.out.println(" End of stream.");
                        return null;
                    }
                    String[] token = line.split(" ");
                    if (token == null || token.length < 2) {
                        return null;
                    }
                    Data data = new Data();
                    data.data = token[0];
                    data.value = Integer.parseInt(token[1]);
                    return data;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        // Step 4.2  Set the data writer.
        fileMerger.setDataWriter(new SortedDataMerger.DataWriter<Data>() {
            @Override
            public void writeData(Writer writer, Data data) {
                String line = data.data + " " + data.value + "\n";
                try {
                    ((BufferedWriter) writer).write(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Step 4.3  Set data merget.
        fileMerger.setDataMerger(new SortedDataMerger.DataMerger<Data>() {
            @Override
            public Data mergeData(Data data1, Data data2) {
                return Data.mergeData(data1, data2);
            }
        });

        // Step 5  Finally run merge command to start the merge process.
        fileMerger.runMerge();


    }
}
