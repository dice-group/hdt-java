package org.rdfhdt.hdt.dictionary.impl;

import sun.jvm.hotspot.utilities.Bits;

import java.io.*;
import java.util.*;

/**
 * Huffman encoding obeys the huffman algorithm.
 * It compresses the input sentence and serializes the "huffman code"
 * and the "tree" used to generate  the huffman code
 * Both the serialized files are intended to be sent to client.
 */
public final class Huffman {

    private Huffman() {
    }

    public static class HuffmanNode {
        char ch;
        int frequency;
        HuffmanNode left;
        HuffmanNode right;

        HuffmanNode(char ch, int frequency, HuffmanNode left, HuffmanNode right) {
            this.ch = ch;
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }
    }

    private static class HuffManComparator implements Comparator<HuffmanNode> {
        @Override
        public int compare(HuffmanNode node1, HuffmanNode node2) {
            return node1.frequency - node2.frequency;
        }
    }

    /**
     * Map<Character, Integer> map
     * Some implementation of that treeSet is passed as parameter.
     *
     * @param map
     */
    public static HuffmanNode buildTree(Map<Character, Integer> map) {
        final Queue<HuffmanNode> nodeQueue = createNodeQueue(map);

        while (nodeQueue.size() > 1) {
            final HuffmanNode node1 = nodeQueue.remove();
            final HuffmanNode node2 = nodeQueue.remove();
            HuffmanNode node = new HuffmanNode('\0', node1.frequency + node2.frequency, node1, node2);
            nodeQueue.add(node);
        }

        // remove it to prevent object leak.
        return nodeQueue.remove();
    }

    private static Queue<HuffmanNode> createNodeQueue(Map<Character, Integer> map) {
        final Queue<HuffmanNode> pq = new PriorityQueue<HuffmanNode>(11, new HuffManComparator());
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue(), null, null));
        }
        return pq;
    }

    public static Map<Character, String> generateCodes(Set<Character> chars, HuffmanNode node) {
        final Map<Character, String> map = new HashMap<Character, String>();
        doGenerateCode(node, map, "");
        return map;
    }


    private static void doGenerateCode(HuffmanNode node, Map<Character, String> map, String s) {
        if (node.left == null && node.right == null) {
            map.put(node.ch, s);
            return;
        }
        doGenerateCode(node.left, map, s + '0');
        doGenerateCode(node.right, map, s + '1');
    }


    private static String encodeMessage(Map<Character, String> charCode, String sentence) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < sentence.length(); i++) {
            stringBuilder.append(charCode.get(sentence.charAt(i)));
        }
        return stringBuilder.toString();
    }

    public static void serializeTree(HuffmanNode node, String filePathTree, String filePathChars) {

        if(new File(filePathTree).exists()){
            new File(filePathTree).delete();
        }
        if(new File(filePathChars).exists()){
            new File(filePathChars).delete();
        }

        final BitSet bitSet = new BitSet();
        try (ObjectOutputStream oosTree = new ObjectOutputStream(new FileOutputStream(filePathTree))) {
            try (ObjectOutputStream oosChar = new ObjectOutputStream(new FileOutputStream(filePathChars))) {
                IntObject o = new IntObject();
                preOrder(node, oosChar, bitSet, o);
                bitSet.set(o.bitPosition, true); // padded to mark end of bit set relevant for deserialization.
                oosTree.writeObject(bitSet);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static class IntObject {
        int bitPosition;
    }

    /*
     * Algo:
     * 1. Access the node
     * 2. Register the value in bit set.
     *
     *
     * here true and false dont correspond to left branch and right branch.
     * there,
     * - true means "a branch originates from leaf"
     * - false mens "a branch originates from non-left".
     *
     * Also since branches originate from some node, the root node must be provided as source
     * or starting point of initial branches.
     *
     * Diagram and how an bit set would look as a result.
     *              (source node)
     *             /             \
     *          true             true
     *           /                  \
     *       (leaf node)        (leaf node)
     *          |                     |
     *        false                  false
     *          |                     |
     *
     * So now a bit set looks like [false, true, false, true]
     *
     */
    private static void preOrder(HuffmanNode node, ObjectOutputStream oosChar, BitSet bitSet, IntObject intObject) throws IOException {
        if (node.left == null && node.right == null) {
            bitSet.set(intObject.bitPosition++, false);  // register branch in bitset
            oosChar.writeChar(node.ch);
            return;                                  // DONT take the branch.
        }
        bitSet.set(intObject.bitPosition++, true);           // register branch in bitset
        preOrder(node.left, oosChar, bitSet, intObject); // take the branch.

        bitSet.set(intObject.bitPosition++, true);               // register branch in bitset
        preOrder(node.right, oosChar, bitSet, intObject);    // take the branch.
    }

    private static void serializeMessage(String message, String filePath) throws IOException {
        final BitSet bitSet = getBitSet(message);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {

            oos.writeObject(bitSet);
        }
    }

    public static void serializeMessages(List<String> messages, String filePath){

        if(new File(filePath).exists()){
            new File(filePath).delete();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            for (String msg : messages) {
                final BitSet bitSet = getBitSet(msg);
                oos.writeObject(bitSet);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static BitSet getBitSet(String message) {
        final BitSet bitSet = new BitSet();
        int i = 0;
        for (; i < message.length(); i++) {
            if (message.charAt(i) == '0') {
                bitSet.set(i, false);
            } else {
                bitSet.set(i, true);
            }
        }
        bitSet.set(i, true); // dummy bit set to know the length
        return bitSet;
    }

    /**
     * Retrieves back the original string.
     *
     * @return The original uncompressed string
     * @throws FileNotFoundException  If the file is not found
     * @throws ClassNotFoundException If class is not found
     * @throws IOException            If IOException occurs
     */
    public static String expand() throws FileNotFoundException, ClassNotFoundException, IOException {
        final HuffmanNode root = deserializeTree();
        return decodeMessage(root);
    }

    private static HuffmanNode deserializeTree() throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream oisBranch = new ObjectInputStream(new FileInputStream("/Users/ap/Desktop/tree"))) {
            try (ObjectInputStream oisChar = new ObjectInputStream(new FileInputStream("/Users/ap/Desktop/char"))) {
                final BitSet bitSet = (BitSet) oisBranch.readObject();
                return preOrder(bitSet, oisChar, new IntObject());
            }
        }
    }

    private static List<BitSet> deserializeMessages(String filePath) {
        List<BitSet> bitSets = new ArrayList<>();
        try (ObjectInputStream oisBranch = new ObjectInputStream(new FileInputStream(filePath))) {
            Object object = oisBranch.readObject();
            while (object != null) {
                bitSets.add((BitSet) object);
                object = oisBranch.readObject();
            }
        } catch (Exception e) {
        }

        return bitSets;
    }

    /*
     * Construct a tree from:
     * input [false, true, false, true, (dummy true to mark the end of bit set)]
     * The input is constructed from preorder traversal
     *
     * Algo:
     * 1  Create the node.
     * 2. Read what is registered in bitset, and decide if created node is supposed to be a leaf or non-leaf
     *
     */
    private static HuffmanNode preOrder(BitSet bitSet, ObjectInputStream oisChar, IntObject o) throws IOException {
        // created the node before reading whats registered.
        final HuffmanNode node = new HuffmanNode('\0', 0, null, null);

        // reading whats registered and determining if created node is the leaf or non-leaf.
        if (!bitSet.get(o.bitPosition)) {
            o.bitPosition++;              // feed the next position to the next stack frame by doing computation before preOrder is called.
            node.ch = oisChar.readChar();
            return node;
        }

        o.bitPosition = o.bitPosition + 1;  // feed the next position to the next stack frame by doing computation before preOrder is called.
        node.left = preOrder(bitSet, oisChar, o);

        o.bitPosition = o.bitPosition + 1; // feed the next position to the next stack frame by doing computation before preOrder is called.
        node.right = preOrder(bitSet, oisChar, o);

        return node;
    }

    private static String decodeMessage(HuffmanNode node) throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/Users/ameya.patil/Desktop/encodedMessage"))) {
            final BitSet bitSet = (BitSet) ois.readObject();
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < (bitSet.length() - 1); ) {
                HuffmanNode temp = node;
                // since huffman code generates full binary tree, temp.right is certainly null if temp.left is null.
                while (temp.left != null) {
                    if (!bitSet.get(i)) {
                        temp = temp.left;
                    } else {
                        temp = temp.right;
                    }
                    i = i + 1;
                }
                stringBuilder.append(temp.ch);
            }
            return stringBuilder.toString();
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        // even number of characters
//        Huffman.compress("some lkdabhsfs sadjf,");
//        Assert.assertEquals("some", Huffman.expand());
//
//        // odd number of characters
//        Huffman.compress("someday");
//        Assert.assertEquals("someday", Huffman.expand());
//
//        // repeating even number of characters + space + non-ascii
//        Huffman.compress("some some#");
//        Assert.assertEquals("some some#", Huffman.expand());
//
//        // odd number of characters + space + non-ascii
//        Huffman.compress("someday someday&");
//        Assert.assertEquals("someday someday&", Huffman.expand());


        List<String> messages = new ArrayList<>();
        messages.add("ab");
        messages.add("adad");
        serializeMessages(messages, "bitset.ser");


        Map<Character,String> mapChar = new HashMap<>();
        mapChar.put('a',"10");
        mapChar.put('b',"1");
        mapChar.put('d',"101");


        List<BitSet> bitSets1 = new ArrayList<>();
        for(String msg : messages){
            String encodeMessage = encodeMessage(mapChar, msg);
            bitSets1.add(getBitSet(encodeMessage));
        }

        List<BitSet> bitSets2 = deserializeMessages("bitset.ser");

        for (int i = 0; i < bitSets1.size(); i++) {
            System.out.println(bitSets1.get(i));
            System.out.println(bitSets2.get(i));
            System.out.println("---");

        }



    }
}