package org.example;
import org.json.JSONObject;
import java.util.HashMap;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class BTreeNode {
    List<JSONObject> books;
    List<BTreeNode> children;
    boolean isLeaf;

    public BTreeNode(boolean isLeaf) {
        this.books = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }

    // Metodo de incertar
    public void insertNonFull(JSONObject book) {
        String isbn = book.getString("isbn");
        int i = this.books.size() - 1;


        if (this.isLeaf) {
            while (i >= 0 && this.books.get(i).getString("isbn").compareTo(isbn) > 0) {
                i--;
            }
            this.books.add(i + 1, book);
        } else {
            while (i >= 0 && this.books.get(i).getString("isbn").compareTo(isbn) > 0) {
                i--;
            }

            if (this.children.get(i + 1).books.size() == 4) {
                this.splitChild(i + 1, this.children.get(i + 1));
                if (this.books.get(i + 1).getString("isbn").compareTo(isbn) < 0) {
                    i++;
                }
            }
            this.children.get(i + 1).insertNonFull(book);
        }
    }

    public void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.isLeaf);

        z.books.addAll(y.books.subList(2, y.books.size()));
        y.books.subList(2, y.books.size()).clear();

        if (!y.isLeaf) {
            z.children.addAll(y.children.subList(2, y.children.size()));
            y.children.subList(2, y.children.size()).clear();
        }

        this.children.add(i + 1, z);
        this.books.add(i, y.books.remove(1));

    }


    public int findKey(String isbn) {
        for (int idx = 0; idx < books.size(); idx++) {
            String currentIsbn = books.get(idx).getString("isbn");
            if (currentIsbn.equals(isbn)) {
                return idx;
            }
            if (currentIsbn.compareTo(isbn) > 0) {
                return idx;
            }
        }
        return books.size();
    }


    public boolean updateBook(String isbn, Map<String, Object> updateData) {
        int idx = findKey(isbn);


        if (idx < books.size() && books.get(idx).getString("isbn").equals(isbn)) {
            JSONObject book = books.get(idx);

            // Update each field in the book
            for (Map.Entry<String, Object> entry : updateData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                book.put(key, value);
            }

            return true;
        }

        if (!isLeaf) {

            if (idx < books.size() && isbn.compareTo(books.get(idx).getString("isbn")) > 0) {
                idx++;
            }

            if (idx < children.size()) {
                return children.get(idx).updateBook(isbn, updateData);
            }
        }

        return false;
    }


    public void removeBook(String isbn) {
        int idx = findKey(isbn);

        if (idx < books.size() && books.get(idx).getString("isbn").compareTo(isbn) == 0) {
            if (isLeaf) {
                books.remove(idx);
            } else {
                removeFromNonLeaf(idx);
            }
        } else {
            if (isLeaf) {
                return;
            }

            boolean flag = (idx == books.size());

            if (children.get(idx).books.size() < 2) {
                fill(idx);
            }

            if (flag && idx > books.size()) {
                children.get(idx - 1).removeBook(isbn);
            } else {
                children.get(idx).removeBook(isbn);
            }
        }
    }

    public void fill(int idx) {
        if (idx != 0 && children.get(idx - 1).books.size() >= 2) {
            borrowFromPrev(idx);
        } else if (idx != books.size() && children.get(idx + 1).books.size() >= 2) {
            borrowFromNext(idx);
        } else {
            if (idx != books.size()) {
                merge(idx);
            } else {
                merge(idx - 1);
            }
        }
    }

    public void removeFromNonLeaf(int idx) {
        String isbnToRemove = books.get(idx).getString("isbn");

        if (children.get(idx).books.size() >= 2) {
            JSONObject pred = getPredecessor(idx);
            books.set(idx, pred);
            children.get(idx).removeBook(pred.getString("isbn"));
        } else if (children.get(idx + 1).books.size() >= 2) {
            JSONObject succ = getSuccessor(idx);
            books.set(idx, succ);
            children.get(idx + 1).removeBook(succ.getString("isbn"));
        }

        else {
            merge(idx);
            children.get(idx).removeBook(isbnToRemove);
        }
    }

    public void borrowFromPrev(int idx) {
        BTreeNode child = children.get(idx);
        BTreeNode sibling = children.get(idx - 1);

        child.books.add(0, books.get(idx - 1));

        books.set(idx - 1, sibling.books.remove(sibling.books.size() - 1));

        if (!child.isLeaf) {
            child.children.add(0, sibling.children.remove(sibling.children.size() - 1));
        }
    }

    public void borrowFromNext(int idx) {
        BTreeNode child = children.get(idx);
        BTreeNode sibling = children.get(idx + 1);

        child.books.add(books.get(idx));

        books.set(idx, sibling.books.remove(0));

        if (!child.isLeaf) {
            child.children.add(sibling.children.remove(0));
        }
    }

    public void merge(int idx) {
        BTreeNode child = children.get(idx);
        BTreeNode sibling = children.get(idx + 1);

        child.books.add(books.remove(idx));

        child.books.addAll(sibling.books);

        if (!child.isLeaf) {
            child.children.addAll(sibling.children);
        }

        children.remove(idx + 1);
    }

    public JSONObject getPredecessor(int idx) {
        BTreeNode current = children.get(idx);
        while (!current.isLeaf) {
            current = current.children.get(current.books.size());
        }
        return current.books.get(current.books.size() - 1);
    }

    public JSONObject getSuccessor(int idx) {
        BTreeNode current = children.get(idx + 1);
        while (!current.isLeaf) {
            current = current.children.get(0);
        }
        return current.books.get(0);
    }

}

class BTree {
    BTreeNode root;
    int t;
    HashMap<String, JSONObject> bookIndexByName;
    HashMap<String, JSONObject> bookIndexByIsbn;

    public BTree() {
        this.t = 5;
        this.root = null;
        this.bookIndexByName = new HashMap<>();
        this.bookIndexByIsbn = new HashMap<>();
    }

    public void insert(JSONObject book) {
        String name = book.getString("name");
        String isbn = book.getString("isbn");


        if (bookIndexByName.containsKey(name)) {
            return;
        }

        if (bookIndexByIsbn.containsKey(isbn)) {
            return;
        }

        bookIndexByName.put(name, book);
        bookIndexByIsbn.put(isbn, book);

        if (root == null) {
            root = new BTreeNode(true);
            root.books.add(book);
        } else {
            if (root.books.size() == 4) {
                BTreeNode newNode = new BTreeNode(false);
                newNode.children.add(root);
                newNode.splitChild(0, root);
                root = newNode;
            }
            root.insertNonFull(book);
        }

    }

    public boolean updateBook(String isbn, Map<String, Object> updateData) {

        if (root != null) {
            JSONObject originalBook = findBookByIsbn(isbn);
            if (originalBook == null) {
                return false;
            }

            String oldName = originalBook.getString("name");

            boolean updated = root.updateBook(isbn, updateData);

            if (updated) {
                JSONObject updatedBook = findBookByIsbn(isbn);

                if (updatedBook != null) {
                    String newName = updatedBook.getString("name");


                    if (!oldName.equals(newName)) {
                        bookIndexByName.remove(oldName);
                        bookIndexByName.put(newName, updatedBook);
                    }

                    bookIndexByIsbn.put(isbn, updatedBook);

                    return true;
                }
            }
        }

        return false;
    }

    public void removeBook(String isbn) {
        if (root != null) {
            JSONObject bookToRemove = findBookByIsbn(isbn);

            if (bookToRemove != null) {
                String name = bookToRemove.getString("name");

                bookIndexByName.remove(name);
                bookIndexByIsbn.remove(isbn);
            }

            root.removeBook(isbn);
            if (root.books.size() == 0) {
                if (root.isLeaf) {
                    root = null;
                } else {
                    root = root.children.get(0);
                }
            }
        }
    }

    public JSONObject searchByName(String name) {
        return bookIndexByName.get(name);
    }

    public JSONObject findBookByIsbn(String isbn) {
        return bookIndexByIsbn.get(isbn);
    }
}

class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;

    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    public int compareTo(HuffmanNode other) {
        return this.frequency - other.frequency;
    }
}

class Huffman {
    public static HuffmanNode buildHuffmanTree(Map<Character, Integer> frequency) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();

        for (Map.Entry<Character, Integer> entry : frequency.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();

            HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;

            pq.add(parent);
        }
        return pq.poll();
    }

    public static void generateCodes(HuffmanNode root, String code, Map<Character, String> huffmanCode) {
        if (root == null)
            return;

        if (root.left == null && root.right == null) {
            huffmanCode.put(root.character, code);
        }

        generateCodes(root.left, code + "0", huffmanCode);
        generateCodes(root.right, code + "1", huffmanCode);
    }
}

class ArithmeticCompressionInt {
    public Map<Character, int[]> probabilities = new HashMap<>();
    public String source;

    private static final int NUMBER_BITS = 16;
    private static final int DEFAULT_LOW = 0;
    private static final int DEFAULT_HIGH = 0xFFFF;
    private static final int MSD = 0x8000;
    private static final int SSD = 0x4000;
    private static final int TOO = 0x3FFF;

    private int scale;

    public ArithmeticCompressionInt(String source) {
        this.source = source;
        calculateProbabilities();
    }

    public ArithmeticCompressionInt(Map<Character, int[]> probabilities, int scale) {
        this.probabilities.putAll(probabilities);
        this.scale = scale;
    }

    private void calculateProbabilities() {
        Map<Character, Integer> frequencies = new HashMap<>();

        for (char symbol : source.toCharArray()) {
            frequencies.put(symbol, frequencies.getOrDefault(symbol, 0) + 1);
        }

        List<Map.Entry<Character, Integer>> sortedFreqs = new ArrayList<>(frequencies.entrySet());
        sortedFreqs.sort((a, b) -> {
            int cmp = a.getValue().compareTo(b.getValue());
            return cmp != 0 ? cmp : Character.compare(a.getKey(), b.getKey());
        });

        scale = source.length();
        int low = 0;

        for (Map.Entry<Character, Integer> symbol : sortedFreqs) {
            int high = low + symbol.getValue();
            probabilities.put(symbol.getKey(), new int[]{low, high});
            low = high;
        }
    }

    public String compress(String input) {
        StringBuilder outputStream = new StringBuilder();
        int low = DEFAULT_LOW;
        int high = DEFAULT_HIGH;
        long underflowBits = 0;

        for (char symbol : input.toCharArray()) {
            long range = (long) (high - low) + 1;
            high = (int) (low + (range * probabilities.get(symbol)[1]) / scale - 1);
            low = (int) (low + (range * probabilities.get(symbol)[0]) / scale);

            while (true) {
                if ((high & MSD) == (low & MSD)) {
                    boolean bit = (high & MSD) != 0;
                    outputStream.append(bit ? "1" : "0");
                    while (underflowBits > 0) {
                        bit = (high & MSD) == 0;
                        outputStream.append(bit ? "1" : "0");
                        underflowBits--;
                    }
                } else {
                    if ((low & SSD) != 0 && (high & SSD) == 0) {
                        underflowBits++;
                        low &= TOO;
                        high |= SSD;
                    } else {
                        break;
                    }
                }
                low <<= 1;
                high <<= 1;
                high |= 1;
                low &= 0xFFFF;
                high &= 0xFFFF;
            }
        }

        boolean finalBit = (low & SSD) != 0;
        outputStream.append(finalBit ? "1" : "0");
        underflowBits++;
        while (underflowBits > 0) {
            boolean bit = (low & SSD) == 0;
            outputStream.append(bit ? "1" : "0");
            underflowBits--;
        }

        if (outputStream.length() % 8 != 0) {
            outputStream.append("0".repeat(8 - outputStream.length() % 8));
        }

        return outputStream.toString();
    }

    public String decompress(String input, int size) throws RuntimeException {
        StringBuilder retval = new StringBuilder();
        int code = 0;
        int low = DEFAULT_LOW;
        int high = DEFAULT_HIGH;

        for (int i = 0; i < NUMBER_BITS; i++) {
            code <<= 1;
            code |= input.charAt(0) == '1' ? 1 : 0;
            input = input.substring(1);
        }

        for (int i = 0; i < size; i++) {
            long range = (long) (high - low) + 1;
            int scaledValue = (int) (((long)(code - low + 1) * scale - 1) / range);

            char c = '\0';
            for (Map.Entry<Character, int[]> symbol : probabilities.entrySet()) {
                if (scaledValue >= symbol.getValue()[0] && scaledValue < symbol.getValue()[1]) {
                    c = symbol.getKey();
                    break;
                }
            }

            if (c == '\0') throw new RuntimeException("Decoding Error");

            retval.append(c);

            range = (long) (high - low) + 1;
            high = (int) (low + (range * probabilities.get(c)[1]) / scale - 1);
            low = (int) (low + (range * probabilities.get(c)[0]) / scale);

            while (true) {
                if ((high & MSD) == (low & MSD)) {
                    // Shift out the most significant bit
                } else {
                    if ((low & SSD) == SSD && (high & SSD) == 0) {
                        code ^= SSD;
                        low &= TOO;
                        high |= SSD;
                    } else {
                        break;
                    }
                }
                low <<= 1;
                high <<= 1;
                high |= 1;
                code <<= 1;
                low &= 0xFFFF;
                high &= 0xFFFF;
                code &= 0xFFFF;
                if (input.length() == 0) break;
                code |= input.charAt(0) == '1' ? 1 : 0;
                input = input.substring(1);
            }
        }
        return retval.toString();
    }


}

public class Main {
    public static void ReaderCSV(String file, BTree tree) {
        String filePath = file;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {

                int separatorIndex = line.indexOf(';');
                if (separatorIndex == -1) {
                    System.err.println("Formato incorrecto: " + line);
                    continue;
                }

                String operation = line.substring(0, separatorIndex).trim();
                String jsonData = line.substring(separatorIndex + 1).trim();

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);

                    switch (operation) {
                        case "INSERT":
                            tree.insert(jsonObject);

                            break;
                        case "PATCH":
                            String isbn = jsonObject.getString("isbn");
                            Map<String, Object> updateData = jsonObject.toMap();
                            updateData.remove("isbn");
                            tree.updateBook(isbn, updateData);

                            break;

                        case "DELETE":
                            String isb = jsonObject.getString("isbn");
                            tree.removeBook(isb);
                            break;
                        default:
                            System.err.println("Operación desconocida: " + operation);
                    }

                } catch (Exception e) {

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int namesize(String name) {
        int size = name.length() * 2;
        return size;
    }

    public static int sumMultiplicationHashMaps(Map<Character, Integer> map1, Map<Character, Integer> map2) {
        int totalSum = 0;

        for (Map.Entry<Character, Integer> entry : map1.entrySet()) {
            char key = entry.getKey();
            int value1 = entry.getValue();
            int value2 = map2.get(key);
            totalSum += value1 * value2;
        }

        return totalSum;
    }

    public static int namesizehuffman(String names) {
        String name = names;
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : name.toCharArray()) {
            frequency.put(c, frequency.getOrDefault(c, 0) + 1);
        }

        Huffman Huff = new Huffman();

        HuffmanNode root = Huff.buildHuffmanTree(frequency);

        Map<Character, String> huffmanCode = new HashMap<>();
        Huff.generateCodes(root, "", huffmanCode);

        Map<Character, Integer> huffmansize = new HashMap<>();

        for (Map.Entry<Character, String> entry : huffmanCode.entrySet()) {
            huffmansize.put(entry.getKey(), entry.getValue().length());
        }

        int sumaHuff = sumMultiplicationHashMaps(frequency, huffmansize);
        return sumaHuff;

    }


    public static int namesizearithmetic(String name) {
        ArithmeticCompressionInt compressor = new ArithmeticCompressionInt(name);
        var size = compressor.compress(name);
        int result = (int) (size.length() / 8);
        return result;
    }




    public static int Equal(int namesize, int sizeHuffman, int sizeArith){
        double newHuff = (double)sizeHuffman/8;
        int sizehuff = (int) Math.ceil(newHuff);
        if(namesize == sizehuff && namesize == sizeArith){
            return 1;
        }
        return 0;
    }

    public static int Decompress(int namesize, int sizeHuffman, int sizeArith){

        double newHuff = (double)sizeHuffman/8;
        int sizehuff = (int) Math.ceil(newHuff);

        if(namesize < sizehuff && namesize < sizeArith){
            return 1;
        }
        return 0;
    }

    public static int Huffman(int namesize, int sizeHuffman, int sizeArith){
        double newHuff = (double)sizeHuffman/8;
        int sizehuff = (int) Math.ceil(newHuff);
        if(sizehuff < namesize && sizehuff < sizeArith){
            return 1;
        }
        return 0;
    }

    public static int Arithmetic(int namesize, int sizeHuffman, int sizeArith){
        double newHuff = (double)sizeHuffman/8;
        int sizehuff = (int) Math.ceil(newHuff);
        if(sizeArith < namesize && sizeArith < sizehuff){
            return 1;
        }
        return 0;
    }

    public static int Either(int sizeHuffman, int sizeArith){
        double newHuff = (double)sizeHuffman/8;
        int sizehuff = (int) Math.ceil(newHuff);
        if(sizehuff == sizeArith){
            return 1;
        }
        return 0;
    }


    public static void Exit(String file, BTree tree) {
        String filepath = file;

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath));
             BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true))) {

            String line;
            int equal = 0;
            int decompress = 0;
            int Huff = 0;
            int arithmetic = 0;
            int either = 0;

            while ((line = reader.readLine()) != null) {
                int separatorIndex = line.indexOf(';');
                if (separatorIndex == -1) {
                    System.err.println("Formato incorrecto: " + line);
                    continue;
                }

                String jsonData = line.substring(separatorIndex + 1).trim();

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    String name = jsonObject.getString("name");

                    JSONObject foundBook = tree.searchByName(name);

                    if (foundBook != null) {
                        String formattedOutput = String.format(
                                "{\"isbn\":\"%s\",\"name\":\"%s\",\"author\":\"%s\",\"price\":\"%s\",\"quantity\":\"%s\", \"namesize\":\"%s\", \"namesizeHuffman\":\"%s\", \"namesizearithmetic\":\"%s\"}",
                                foundBook.getString("isbn"),
                                foundBook.getString("name"),
                                foundBook.getString("author"),
                                foundBook.getString("price"),
                                foundBook.getString("quantity"),
                                namesize(foundBook.getString("name")),
                                namesizehuffman(foundBook.getString("name")),
                                namesizearithmetic(foundBook.getString("name")));
                        equal += Equal(namesize(foundBook.getString("name")), namesizehuffman(foundBook.getString("name")), namesizearithmetic(foundBook.getString("name")));

                        decompress += Decompress(namesize(foundBook.getString("name")), namesizehuffman(foundBook.getString("name")), namesizearithmetic(foundBook.getString("name")));

                        Huff +=Huffman(namesize(foundBook.getString("name")), namesizehuffman(foundBook.getString("name")), namesizearithmetic(foundBook.getString("name")));

                        arithmetic += Arithmetic(namesize(foundBook.getString("name")), namesizehuffman(foundBook.getString("name")), namesizearithmetic(foundBook.getString("name")));

                        either += Either(namesizehuffman(foundBook.getString("name")), namesizearithmetic(foundBook.getString("name")));
                        writer.write(formattedOutput);
                        writer.newLine();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            writer.write("Equal: " + equal);
            writer.newLine();
            writer.write("Decompress: " + decompress);
            writer.newLine();
            writer.write("Huffman: " + Huff);
            writer.newLine();
            writer.write("Arithmetic: " + arithmetic);
            writer.newLine();
            writer.write("Either: " + either);
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String file = "lab01_books.csv";
        String file2 = "lab01_search.csv";
        BTree tree = new BTree();
        // Insertador, actualizando y eliminando libros en el arbol
        ReaderCSV(file, tree);
        // Despues de haber insertado, actulizado y eliminado libros en el arbol
        Exit(file2, tree);

    }
}