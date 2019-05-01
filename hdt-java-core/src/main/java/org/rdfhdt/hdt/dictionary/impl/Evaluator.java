package org.rdfhdt.hdt.dictionary.impl;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {

    public static final boolean HUFFMAN_ACTIVE = true;
    public static final boolean BLANK_SHORT_ACTIVE = false;
    public static final boolean BLANK_OMIT_ACTIVE = false;

    private static final Map<String, String> mapSuffixToFormat = new HashMap<>();

    static {
        final String NTRIPLES = "ntriples";
        final String RDF_XML = "rdf-xml";
        mapSuffixToFormat.put("ttl", NTRIPLES);
        mapSuffixToFormat.put("nt", NTRIPLES);
        mapSuffixToFormat.put("inf", NTRIPLES);
        mapSuffixToFormat.put("rdf", RDF_XML);
    }


    public static void main(String[] a) {
        List<String> files = new ArrayList<>();
        File dir = new File("/Users/philipfrerk/Downloads/semantic_weg_dog_food");
        for(File f : dir.listFiles()){
            files.add(f.getAbsolutePath());
        }


        List<CompressionResult> results = evaluateFiles(files);
//        List<CompressionResult> results = evaluateFiles(new String[]{"/Users/philipfrerk/Downloads/semantic_weg_dog_food/iswc-2010-complete-alignments.rdf"});

        System.out.println("File names: ");
        for (CompressionResult result : results) {
            System.out.print("\'"+result.getFile().getName() + "\',");
        }

        System.out.println("\n\nCompr ratios: ");
        for (CompressionResult result : results) {
            System.out.print(result.getCompressionRatio() + ",");
        }

        System.out.println("\n\nCompr times: ");
        for (CompressionResult result : results) {
            System.out.print(result.getCompressionTime() + ",");
        }
    }

    private static long getFileLength(String file) {
        File f = new File(file);
        if (!f.exists()) {
            throw new RuntimeException("file not existing: " + file);
        }
        f.delete();
        return f.length();
    }

    private static String getFileSuffix(String filePath) {
        String[] splitted = filePath.split("\\.");
        return splitted[splitted.length - 1];
    }


    private static List<CompressionResult> evaluateFiles(List<String> files) {
        String[] array = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            array[i]=files.get(i);
        }
       return evaluateFiles(array);
    }

    private static List<CompressionResult> evaluateFiles(String[] files) {
        List<CompressionResult> results = new ArrayList<>();
        for (String fileInput : files) {

            String output = "file.hdt";
            File fileOutput = new File(output);
            if (fileOutput.exists()) {
                fileOutput.delete();
            }

            long comprTime;
            if (HUFFMAN_ACTIVE) {
                comprTime = System.currentTimeMillis();
                HuffmanFacade.findCharacterCounts(fileInput);
            } else {
                comprTime = System.currentTimeMillis();
            }

            String baseURI = "http://example.com/mydataset";
            String inputType = mapSuffixToFormat.get(getFileSuffix(fileInput));

            try {
                HDT hdt = HDTManager.generateHDT(
                        fileInput,         // Input RDF File
                        baseURI,          // Base URI
                        RDFNotation.parse(inputType), // Input Type
                        new HDTSpecification(),   // HDT Options
                        null              // Progress Listener
                );

                hdt.saveToHDT(output, null);
                comprTime = System.currentTimeMillis() - comprTime;


            } catch (Exception e) {
                e.printStackTrace();
                results.add(null);
                continue;
            }

            long outputLength = 0;
            outputLength += fileOutput.length();
            fileOutput.delete();


            if (BLANK_SHORT_ACTIVE && BLANK_OMIT_ACTIVE) {
                outputLength += getFileLength(BlankNodesManager.BLANK_NODES_FILE);
            }

            // literals
            if (HUFFMAN_ACTIVE) {
                outputLength += getFileLength(HuffmanFacade.FILE_LITERALS);
                outputLength += getFileLength(HuffmanFacade.FILE_TREE);
                outputLength += getFileLength(HuffmanFacade.FILE_CHARS);
            }

            File inputFile = new File(fileInput);
            long inputLength = inputFile.length();

            results.add(new CompressionResult(inputLength, outputLength, -1, comprTime,
                    -1, inputFile));

        }
        return results;
    }
}
