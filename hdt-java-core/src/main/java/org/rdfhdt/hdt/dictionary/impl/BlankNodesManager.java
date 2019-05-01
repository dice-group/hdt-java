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



}

