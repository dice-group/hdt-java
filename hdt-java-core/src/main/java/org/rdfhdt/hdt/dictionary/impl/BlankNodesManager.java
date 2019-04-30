package org.rdfhdt.hdt.dictionary.impl;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.util.string.CompactString;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlankNodesManager {

    public static final boolean HUFFMAN_ACTIVE = true;

    private static long count = 0;
    private static final Map<CharSequence,CharSequence> mapOldToNewId = new HashMap<>();

    public static final String BLANK_NODES_FILE = "blank";


    public static CharSequence getNewId(CharSequence oldId){
        if(mapOldToNewId.containsKey(oldId)){
            return mapOldToNewId.get(oldId);
        }else{
            if(oldId instanceof String) {
                mapOldToNewId.put(oldId, String.valueOf(count++));
                return String.valueOf(count);
            }else if(oldId instanceof CompactString){
                CompactString cs = new CompactString(String.valueOf(count));
                mapOldToNewId.put(oldId, cs);
                count++;
                return cs;
            }
            throw new IllegalArgumentException("Illegal input class: "+oldId.getClass());
        }
    }


    public static void main(String[] a){
        String baseURI = "http://example.com/mydataset";
        String rdfInput = "/Users/philipfrerk/Downloads/page-length_en_small.ttl";

        String inputType = "turtle"; //todo: careful
        String output = "file.hdt";

        File file = new File(output);
        if(file.exists()){
            file.delete();
        }


        if(HUFFMAN_ACTIVE) {
            HuffmanCodeGenerator.findCharacterCounts(rdfInput);
        }
        try {

            HDT hdt = HDTManager.generateHDT(
                    rdfInput,         // Input RDF File
                    baseURI,          // Base URI
                    RDFNotation.parse(inputType), // Input Type
                    new HDTSpecification(),   // HDT Options
                    null              // Progress Listener
            );

            hdt.saveToHDT(output, null);



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }

        long length = 0;
        length+=new File(output).length();

        File f = new File(BLANK_NODES_FILE);
        if(f.exists()) {
            length += f.length();
        }

        // literals
        File fileLiterals = new File(HuffmanCodeGenerator.FILE_LITERALS);
        if(fileLiterals.exists()){
            length+=fileLiterals.length();
        }
        File fileTree = new File(HuffmanCodeGenerator.FILE_TREE);
        if(fileTree.exists()){
            length+=fileTree.length();
        }

        File fileChars = new File(HuffmanCodeGenerator.FILE_CHARS);
        if(fileChars.exists()){
            length+=fileChars.length();
        }


        System.out.println("compr size: "+length);
        System.out.println("compr ratio: "+ 1.0*length / new File(rdfInput).length());


    }
}

