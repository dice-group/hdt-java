package org.rdfhdt.hdt.dictionary.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Analyzer {

    public static FileInfo getInformation(String file){
        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int literalCount=0, literalLengthSum=0, blankNodeCount=0, blankNodeIdLengthSum=0;

        ExtendedIterator<Triple> tripleExtendedIterator = model.getGraph().find();
        while (tripleExtendedIterator.hasNext()) {
            Triple triple = tripleExtendedIterator.next();
            Node subject = triple.getSubject();
            Node object = triple.getObject();

            if(subject.isBlank()){
                blankNodeCount++;
                blankNodeIdLengthSum+=subject.getBlankNodeId().toString().length();
            }

            if(object.isLiteral()){
                literalCount++;
                literalLengthSum+=object.getLiteral().toString().length();
            }else if(object.isBlank()){
                blankNodeCount++;
                blankNodeIdLengthSum+=object.getBlankNodeId().toString().length();
            }
        }

        double size = model.getGraph().size();

        double literalAvgLength = 1.0*literalLengthSum/size;
        double blankIdAvgLength = 1.0*blankNodeIdLengthSum/size;

        return new FileInfo(literalCount/size, blankNodeCount/size,
                literalAvgLength,blankIdAvgLength);
    }



    public static class FileInfo{
        public double literalRelativeAmount, blankNodeRelativeAmount;
        public double literalAvgLength, blankNodeAvgLength;

        public FileInfo(double literalRelativeAmount, double blankNodeRelativeAmount, double literalAvgLength, double blankNodeAvgLength) {
            this.literalRelativeAmount = literalRelativeAmount;
            this.blankNodeRelativeAmount = blankNodeRelativeAmount;
            this.literalAvgLength = literalAvgLength;
            this.blankNodeAvgLength = blankNodeAvgLength;
        }
    }

    public static void main(String[] a){
        List<String> files = new ArrayList<>();
        File dir = new File("/Users/philipfrerk/Documents/RDF_data/DBPedia_abstracts");
        File[] filesOfDirectory = Evaluator.listSortedFilesOfDirectory(dir);

        for(File f : filesOfDirectory){
            if(!f.getName().toLowerCase().contains("ds_store")) {
                files.add(f.getAbsolutePath());
            }
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for(String file : files){
            fileInfos.add(getInformation(file));
        }

        System.out.println("Literal percentage: ");
        for(FileInfo fileInfo:fileInfos){
            System.out.print(fileInfo.literalRelativeAmount+",");
        }

        System.out.println("\n\nLiteral avg length: ");
        for(FileInfo fileInfo:fileInfos){
            System.out.print(fileInfo.literalAvgLength+",");
        }

        System.out.println("\n\nFiles: ");
        for(String fileInfo:files){
            System.out.print(fileInfo+",");
        }
    }
}
