package bplustree;
import org.junit.Test;

public class BPlusTreeTest {
    @Test
    public void testRotation() {
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");
        tree.insert(5, "12345");
        tree.insert(6, "123456");

        tree.visualize("rotation-before");

        tree.insert(7, "1234567");

        tree.visualize("rotation-after");
    }

    @Test
    public void testSplitLeafNode() {
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");

        tree.visualize("splitLeafNode-before");

        tree.insert(5, "12345");

        tree.visualize("splitLeafNode-after");
    }

    @Test
    public void testSplitInternalNode() {
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");
        tree.insert(5, "12345");
        tree.insert(6, "123456");
        tree.insert(7, "1234567");
        tree.insert(8, "12345678");
        tree.insert(9, "123456789");
        tree.insert(10, "123456789");
        tree.insert(11, "123456789");
        tree.insert(12, "123456789");
        tree.insert(13, "123456789");
        tree.insert(14, "123456789");
        tree.insert(15, "123456789");
        tree.insert(16, "123456789");
        tree.insert(17, "123456789");
        tree.insert(18, "123456789");
        tree.insert(19, "123456789");
        tree.insert(20, "123456789");

        tree.visualize("splitInternalNode-before");

        tree.insert(21, "123456789");

        tree.visualize("splitInternalNode-after");
    }

    @Test
    public void testDeleteLeafNode() {
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");
        tree.insert(5, "12345");

        tree.visualize("deleteLeafNode-before");

        tree.delete(5);

        tree.visualize("deleteLeafNode-after");
    }

    @Test
    public void testDeleteIndexNode() {
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");
        tree.insert(5, "12345");

        tree.visualize("deleteIndexNode-before");

        tree.delete(3);

        tree.visualize("deleteIndexNode-after");
    }

    @Test
    public void testMergeLeafNode() {
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");
        tree.insert(5, "12345");

        tree.visualize("mergeLeafNode-before");

        tree.delete(1);

        tree.visualize("mergeLeafNode-after");
    }

    @Test
    public void testMergeInternalNode() {
        // 有 bug，原因是删除索引中的 key 时合并两个子节点，一个子节点声明的空间不够用，需要加倍。
        // 同时也要更改 underflow, overflow, canlendakey 的标准要改变成利用 order 来判断
        BPlusTree<Integer, String> tree = new BPlusTree<>();

        tree.insert(1, "1");
        tree.insert(2, "12");
        tree.insert(3, "123");
        tree.insert(4, "1234");
        tree.insert(5, "12345");
        tree.insert(6, "123456");
        tree.insert(7, "1234567");
        tree.insert(8, "12345678");
        tree.insert(9, "123456789");
        tree.insert(10, "123456789");
        tree.insert(11, "123456789");
        tree.insert(12, "123456789");
        tree.insert(13, "123456789");
        tree.insert(14, "123456789");
        tree.insert(15, "123456789");
        tree.insert(16, "123456789");
        tree.insert(17, "123456789");
        tree.insert(18, "123456789");
        tree.insert(19, "123456789");
        tree.insert(20, "123456789");
        tree.insert(21, "123456789");
        tree.insert(22, "123456789");
        tree.insert(23, "123456789");

        tree.visualize("mergeInternalNode-before");

        tree.delete(20);

        tree.visualize("mergeInternalNode-after");
    }
}
