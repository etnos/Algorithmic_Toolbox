import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SetRangeSum {

    BufferedReader br;
    PrintWriter out;
    StringTokenizer st;
    boolean eof;

    // Splay tree implementation

    // Vertex of a splay tree
    class Vertex {
        int key;
        // Sum of all the keys in the subtree - remember to update
        // it after each operation that changes the tree.
        long sum;
        Vertex left;
        Vertex right;
        Vertex parent;

        Vertex(int key, long sum, Vertex left, Vertex right, Vertex parent) {
            this.key = key;
            this.sum = sum;
            this.left = left;
            this.right = right;
            this.parent = parent;
        }
    }

    void update(Vertex v) {
        if (v == null) return;
        v.sum = v.key + (v.left != null ? v.left.sum : 0) + (v.right != null ? v.right.sum : 0);
        if (v.left != null) {
            v.left.parent = v;
        }
        if (v.right != null) {
            v.right.parent = v;
        }
    }

    void smallRotation(Vertex v) {
        Vertex parent = v.parent;
        if (parent == null) {
            return;
        }
        Vertex grandparent = v.parent.parent;
        if (parent.left == v) {
            Vertex m = v.right;
            v.right = parent;
            parent.left = m;
        } else {
            Vertex m = v.left;
            v.left = parent;
            parent.right = m;
        }
        update(parent);
        update(v);
        v.parent = grandparent;
        if (grandparent != null) {
            if (grandparent.left == parent) {
                grandparent.left = v;
            } else {
                grandparent.right = v;
            }
        }
    }

    void bigRotation(Vertex v) {
        if (v.parent.left == v && v.parent.parent.left == v.parent) {
            // Zig-zig
            smallRotation(v.parent);
            smallRotation(v);
        } else if (v.parent.right == v && v.parent.parent.right == v.parent) {
            // Zig-zig
            smallRotation(v.parent);
            smallRotation(v);
        } else {
            // Zig-zag
            smallRotation(v);
            smallRotation(v);
        }
    }

    // Makes splay of the given vertex and returns the new root.
    Vertex splay(Vertex v) {
        if (v == null) return null;
        while (v.parent != null) {
            if (v.parent.parent == null) {
                smallRotation(v);
                break;
            }
            bigRotation(v);
        }
        return v;
    }

    class VertexPair {
        Vertex left;
        Vertex right;

        VertexPair() {
        }

        VertexPair(Vertex left, Vertex right) {
            this.left = left;
            this.right = right;
        }
    }

    // Searches for the given key in the tree with the given root
    // and calls splay for the deepest visited node after that.
    // Returns pair of the result and the new root.
    // If found, result is a pointer to the node with the given key.
    // Otherwise, result is a pointer to the node with the smallest
    // bigger key (next value in the order).
    // If the key is bigger than all keys in the tree,
    // then result is null.
    VertexPair find(Vertex root, int key) {
        Vertex v = root;
        Vertex last = root;
        Vertex next = null;
        while (v != null) {
            if (v.key >= key && (next == null || v.key < next.key)) {
                next = v;
            }
            last = v;
            if (v.key == key) {
                break;
            }
            if (v.key < key) {
                v = v.right;
            } else {
                v = v.left;
            }
        }
        root = splay(last);
        return new VertexPair(next, root);
    }

    VertexPair split(Vertex root, int key) {
        VertexPair result = new VertexPair();
        VertexPair findAndRoot = find(root, key);
        root = findAndRoot.right;
        result.right = findAndRoot.left;
        if (result.right == null) {
            result.left = root;
            return result;
        }
        result.right = splay(result.right);
        result.left = result.right.left;
        result.right.left = null;
        if (result.left != null) {
            result.left.parent = null;
        }
        update(result.left);
        update(result.right);
        return result;
    }

    Vertex merge(Vertex left, Vertex right) {
        if (left == null) return right;
        if (right == null) return left;
        while (right.left != null) {
            right = right.left;
        }
        right = splay(right);
        right.left = left;
        update(right);
        return right;
    }

    // Code that uses splay tree to solve the problem

    Vertex root = null;

    void insert(int x) {
        Vertex left = null;
        Vertex right = null;
        Vertex new_vertex = null;
        VertexPair leftRight = split(root, x);
        left = leftRight.left;
        right = leftRight.right;
        if (right == null || right.key != x) {
            new_vertex = new Vertex(x, x, null, null, null);
        }
        root = merge(merge(left, new_vertex), right);
    }

    void erase(int x) {
        // Implement erase yourself
        VertexPair pairLeft = split(root, x);
        VertexPair pairRight = split(pairLeft.right, x + 1);
        root = merge(pairLeft.left, pairRight.right);
        splay(root);
    }

    boolean find(int x) {
        // Implement find yourself
        if (root != null) {
            VertexPair n = find(root, x);
            root = splay(n.right);
            return root.key == x;
        }
        return false;
    }

    long sum(int from, int to) {
        VertexPair leftMiddle = split(root, from);
        Vertex left = leftMiddle.left;
        Vertex middle = leftMiddle.right;
        VertexPair middleRight = split(middle, to + 1);
        middle = middleRight.left;
        Vertex right = middleRight.right;
        long ans = 0;
        // Complete the implementation of sum
        if (middle == null) {
            ans = 0;
            root = merge(left, right);
        } else {
            ans = middle.sum;
            root = merge(merge(left, middle), right);
        }
        return ans;
    }


    public static final int MODULO = 1000000001;

    void solve() throws IOException {
        int n = nextInt();
        int last_sum_result = 0;
        for (int i = 0; i < n; i++) {
            char type = nextChar();
            switch (type) {
                case '+': {
                    int x = nextInt();
                    insert((x + last_sum_result) % MODULO);
                }
                break;
                case '-': {
                    int x = nextInt();
                    erase((x + last_sum_result) % MODULO);
                }
                break;
                case '?': {
                    int x = nextInt();
                    out.println(find((x + last_sum_result) % MODULO) ? "Found" : "Not found");
                }
                break;
                case 's': {
                    int l = nextInt();
                    int r = nextInt();
                    long res = sum((l + last_sum_result) % MODULO, (r + last_sum_result) % MODULO);
                    out.println(res);
                    last_sum_result = (int) (res % MODULO);
                }
            }
        }
    }

//    SetRangeSum() throws IOException {
//        br = new BufferedReader(new InputStreamReader(System.in));
//        out = new PrintWriter(System.out);
//        solve();
//        out.close();
//    }

    SetRangeSum(BufferedReader bufferedReader, PrintWriter printWriter) throws IOException {
        br = bufferedReader;
        out = printWriter;
    }

    public static void main(String[] args) throws IOException {
//        new SetRangeSum();

        autoTest();
    }

    String nextToken() {
        while (st == null || !st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (Exception e) {
                eof = true;
                return null;
            }
        }
        return st.nextToken();
    }

    int nextInt() throws IOException {
        return Integer.parseInt(nextToken());
    }

    char nextChar() throws IOException {
        return nextToken().charAt(0);
    }


    private static void autoTest() {
        try {
            testCase("src/assignment4/set_range_sums/tests/01", "src/assignment4/set_range_sums/tests/01.a", "01");
            testCase("src/assignment4/set_range_sums/tests/04", "src/assignment4/set_range_sums/tests/04.a", "04");
            testCase("src/assignment4/set_range_sums/tests/05", "src/assignment4/set_range_sums/tests/05.a", "05");
            testCase("src/assignment4/set_range_sums/tests/20", "src/assignment4/set_range_sums/tests/20.a", "20");
            testCase("src/assignment4/set_range_sums/tests/36", "src/assignment4/set_range_sums/tests/36.a", "36");
            testCase("src/assignment4/set_range_sums/tests/83", "src/assignment4/set_range_sums/tests/83.a", "83");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testCase(String data, String expectedResult, String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(data)));
        String tempResultFile = "src/assignment4/set_range_sums/tempResult.txt";
        File file = new File(tempResultFile);
        file.getParentFile().mkdirs();
        PrintWriter printWriter = new PrintWriter(file);

        SetRangeSum setRangeSum = new SetRangeSum(bufferedReader, printWriter);
        setRangeSum.solve();
        printWriter.close();

        if (!isEqual(expectedResult, tempResultFile)) {
            System.out.println("WRONG " + fileName);
        }
    }

    private static boolean isEqual(String file1, String file2) throws IOException {

        String dataFile1 = new String(Files.readAllBytes(Paths.get(file1)));
        String dataFile2 = new String(Files.readAllBytes(Paths.get(file2)));

        dataFile1 = dataFile1.replaceAll("[^\\p{L}\\p{Z}]", "");
        dataFile2 = dataFile2.replaceAll("[^\\p{L}\\p{Z}]", "");

        return dataFile1.equals(dataFile2);
    }
}
