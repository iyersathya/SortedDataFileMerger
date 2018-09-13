package com.merger;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class will take list of input readers and merge them to create a single
 * output writer.
 * input readers are the files that may contain already sorted content with some sorting criteria
 * ( as defined by Comparator<T> passed in to ctor)
 * output will be one single file with all the contents from input readers sorted.
 * @param <T>
 */
public class SortedDataMerger<T> {

    /**
     * Interface that will create a single data of type T
     * form reading data from reader object.
     * @param <T>
     */
    public interface DataReader<T> {
        T readData(Reader reader);
    }

    /**
     * Interface that will write single data T to writer object.
     * @param <T>
     */
    public interface DataWriter<T> {
        void writeData(Writer writer, T data);
    }

    /**
     * Merge two data T objects and return a new data object.
     * For example merge merge may simply add frequency of two data T
     *
     * @param <T>
     */
    public interface DataMerger<T> {
        T mergeData(T data1, T data2);
    }

    /**
     * Internal Node class that will form a entry of PriorityQueue.
     */
    private class Node {
        public T data;
        public Reader reader;

        @Override
        public String toString() {
            return data.toString();
        }
    }


    /**
     * Private variables.
     */
    private PriorityQueue<Node> mPriorityQ;
    private List<Reader> mInputReaders;
    private DataReader<T> mDataReader;
    private DataMerger<T> mDataMerger;
    private DataWriter<T> mDataWriter;
    private Writer mOutputWriter;
    private Comparator<T> mComparator;


    /**
     * Create new Instance of this class.
     * @param inputReaders
     * @param outputWriter
     * @param comparator
     */
    public SortedDataMerger(List<Reader> inputReaders,
                            Writer outputWriter, Comparator<T> comparator) {
        if (inputReaders == null || inputReaders.size() == 0 || outputWriter == null) {
            throw new IllegalArgumentException(" Input parameters invalid");
        }

        mInputReaders = inputReaders;
        mOutputWriter = outputWriter;
        if (comparator != null) {
            mComparator = comparator;
            mPriorityQ = new PriorityQueue(inputReaders.size(), new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return mComparator.compare(o1.data, o2.data);
                }
            });
        } else {
            mPriorityQ = new PriorityQueue(inputReaders.size());
        }

    }

    /**
     * Set data reader
     * @param dataReaderer
     */
    public void setDataReader(DataReader<T> dataReaderer) {
        mDataReader = dataReaderer;
    }

    /**
     * set data merger
     * @param dataMerger
     */
    public void setDataMerger(DataMerger<T> dataMerger) {
        mDataMerger = dataMerger;
    }

    /**
     * Set data writer.
     * @param dataWriter
     */
    public void setDataWriter(DataWriter<T> dataWriter) {
        mDataWriter = dataWriter;
    }

    /**
     * Read new data from reader.
     * @param reader
     * @return
     */
    private Node readData(Reader reader) {
        T data = mDataReader.readData(reader);
        if (data == null) {
            return null;
        }
        Node node = new Node();
        node.data = data;
        node.reader = reader;
        return node;
    }

    /**
     * Remove data from top of priority queue and add new data to PQ.
     * @return
     */
    private Node getNextNode() {
        Node node = mPriorityQ.remove();
        if (node == null) {
            return null;
        }
//        System.out.println(" Processing: Node:" + node.toString());
        Node newNode = readData(node.reader);
        if (newNode != null) {
            mPriorityQ.add(newNode);
        }
        return node;
    }

    /**
     * Merge two nodes.
     * @param peekNode
     * @param removedNode
     */
    private void mergeNodes(Node peekNode, Node removedNode) {
        T newData = mDataMerger.mergeData(peekNode.data, removedNode.data);
        peekNode.data = newData;
    }

    /**
     * Run merge logic that will do the process of merging the files.
     */
    public void runMerge() {
        // Read and heapify PriorityQ.
        for (Reader reader : mInputReaders) {
            Node node = readData(reader);
            if (node != null) {
                mPriorityQ.add(node);
            }
        }

        // Start the merge process.
        while (!mPriorityQ.isEmpty()) {
            Node node = getNextNode();
            if (node == null) {
                continue;
            }
            Node peek = mPriorityQ.peek();
            if (peek != null) {
                if (mComparator.compare(node.data, peek.data) == 0) {
                    mergeNodes(peek, node);
                } else {
                    mDataWriter.writeData(mOutputWriter, node.data);
                }
            } else {
                mDataWriter.writeData(mOutputWriter, node.data);
            }
        }
        try {
            mOutputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
