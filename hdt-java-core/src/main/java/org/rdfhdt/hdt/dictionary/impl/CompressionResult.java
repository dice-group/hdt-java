package org.rdfhdt.hdt.dictionary.impl;

import java.io.File;

public class CompressionResult implements Comparable {

    private long originalSize, compressedSize, compressionDictionarySize, compressionTime, decompressionTime;

    private File file;

    public CompressionResult(long originalSize, long compressedSize, long compressionDictionarySize, long compressionTime, long decompressionTime, File file) {
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionDictionarySize = compressionDictionarySize;
        this.compressionTime = compressionTime;
        this.decompressionTime = decompressionTime;
        this.file = file;
    }

    @Override
    public String toString() {
        return  "Compression Result: {\n"+
                "file name: "+ file.getName() + "\n"+
                "original size: " + originalSize + "\n"+
                "compressed size: " + compressedSize + "\n"+
                "compressed dict size: " + compressionDictionarySize + "\n"+
                "compression ratio: " + getCompressionRatio()+"\n"+
                "compression time: "+compressionTime+"\n"+
                "decompressionTime: " + decompressionTime+"\n"
                +"}\n";
    }

    public double getCompressionRatio(){
        return 1.0*compressedSize/originalSize;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getCompressionDictionarySize() {
        return compressionDictionarySize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public long getCompressionTime() {
        return compressionTime;
    }

    public long getDecompressionTime() {
        return decompressionTime;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(Object o) {
        CompressionResult result = (CompressionResult) o;
        if (getCompressionRatio() == result.getCompressionRatio()) {
            return 0;
        }
        if (getCompressionRatio() < result.getCompressionRatio()) {
            return -1;
        } else {
            return 1;
        }
    }
}
