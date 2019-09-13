package bplustree;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A B+ tree
 * Since the structures and behaviors between internal node and external node
 * are different,
 * so there are two different classes for each kind of node.
 * 
 * @param <TKey>
 *            the data type of the key
 * @param <TValue>
 *            the data type of the value
 */

public class BPlusTree<TKey extends Comparable<TKey>, TValue> {
    private BPlusTreeNode<TKey> root;
    private final static int ORDER = 4;

    public BPlusTree() {
        // The root node starts as a leaf node with zero key/value pairs
        this.root = new BPlusTreeLeafNode<TKey, TValue>();
    }

    @SuppressWarnings("unchecked")
    private BPlusTreeLeafNode<TKey, TValue> findLeafNode(TKey key) {
        // 找到包含该 key 的叶节点
        BPlusTreeNode<TKey> node = this.root;

        while (node.getNodeType() != BPlusTreeNodeType.LeafNode) {
            int target = node.find(key);
            node = ((BPlusTreeInternalNode<TKey>) node).getChild(target);
        }

        return (BPlusTreeLeafNode<TKey, TValue>) node;
    }

    private BPlusTreeInternalNode<TKey> findInternalNode(TKey key) {
        // 找到包含该 key 的内节点
        BPlusTreeNode<TKey> node = this.root;

        while (node.getNodeType() != BPlusTreeNodeType.LeafNode) {
            int target = node.find(key);

            if (target != 0 && node.getKey(target - 1).compareTo(key) == 0) {
                return (BPlusTreeInternalNode<TKey>) node;
            }

            node = ((BPlusTreeInternalNode<TKey>) node).getChild(target);
        }

        return null;
    }

    /**
     * Insert a key and value pair to the B Plus Tree
     *
     * @param key
     *            the key to be inserted
     * @param value
     *            the value to be inserted
     */
    public void insert(TKey key, TValue value) {
        // 先添加到叶子节点中，然后判断该叶子是否满
        BPlusTreeLeafNode<TKey, TValue> leafNode = findLeafNode(key);
        leafNode.insert(key, value);

        if (leafNode.isOverflow() && leafNode.getParentNode() != null) {
            // Support rotate function
            BPlusTreeInternalNode<TKey> parentNode = (BPlusTreeInternalNode<TKey>) leafNode.getParentNode();
            parentNode.rotate(leafNode);
        }

        if (leafNode.isOverflow()) {
            BPlusTreeNode<TKey> node = leafNode.handleOverflow();
            if (node != null) {
                this.root = node;
            }
        }
    }

    /**
     * Search a key value on the tree and return its associated value.
     */
    public TValue search(TKey key) {
        BPlusTreeLeafNode<TKey, TValue> leaf = findLeafNode(key);

        int index = leaf.find(key);

        return (index == -1) ? null : leaf.getValue(index);
    }

    /**
     * Delete a key and its associated value from the tree.
     */
    @SuppressWarnings("unchecked")
    public void delete(TKey key) {
        BPlusTreeLeafNode<TKey, TValue> leafNode = findLeafNode(key);

        if (leafNode.delete(key) && leafNode.isUnderflow()) {
            BPlusTreeNode<TKey> node = leafNode.handleUnderflow();

            if (node != null) {
                this.root = node;
            }
        }

        /* 检查 key 是否在索引中出现 */
        BPlusTreeInternalNode<TKey> internalNode = findInternalNode(key);

        if (internalNode != null) {
            int index = internalNode.find(key) - 1;

            // 将待删除索引节点的右孩子合并到左孩子中
            if (internalNode.getChild(index).getNodeType() == BPlusTreeNodeType.InternalNode) {
                BPlusTreeInternalNode<TKey> leftSiblingNode = (BPlusTreeInternalNode<TKey>) internalNode
                        .getChild(index);
                BPlusTreeInternalNode<TKey> rightSiblingNode = (BPlusTreeInternalNode<TKey>) internalNode
                        .getChild(index + 1);

                int j = leftSiblingNode.getKeyCount();

                for (int i = 0; i < rightSiblingNode.getKeyCount(); i++) {
                    leftSiblingNode.setKey(j + i, rightSiblingNode.getKey(i));
                }

                for (int i = 0; i < rightSiblingNode.getKeyCount() + 1; i++) {
                    leftSiblingNode.setChild(j + i, rightSiblingNode.getChild(i));
                }

                leftSiblingNode.keyCount += rightSiblingNode.getKeyCount();

                leftSiblingNode.setRightSibling(rightSiblingNode.rightSibling);
                if (rightSiblingNode.rightSibling != null) {
                    rightSiblingNode.rightSibling.setLeftSibling(leftSiblingNode);
                }
            } else {
                BPlusTreeLeafNode<TKey, TValue> leftSiblingNode = (BPlusTreeLeafNode<TKey, TValue>) internalNode
                        .getChild(index);
                BPlusTreeLeafNode<TKey, TValue> rightSiblingNode = (BPlusTreeLeafNode<TKey, TValue>) internalNode
                        .getChild(index + 1);

                // 简化
                // leftSiblingNode.fuseWithSibling(null, rightSiblingNode);

                int j = leftSiblingNode.getKeyCount();

                for (int i = 0; i < rightSiblingNode.getKeyCount(); i++) {
                    leftSiblingNode.setKey(j + i, rightSiblingNode.getKey(i));
                    leftSiblingNode.setValue(j + i, rightSiblingNode.getValue(i));
                }

                leftSiblingNode.keyCount += rightSiblingNode.getKeyCount();

                leftSiblingNode.setRightSibling(rightSiblingNode.rightSibling);
                if (rightSiblingNode.rightSibling != null) {
                    rightSiblingNode.rightSibling.setLeftSibling(leftSiblingNode);
                }
            }

            /*
             * 以下可以化简成：
             * 1. 如果被删除 key 的叶节点没有 underflow
             * 1.1 用该叶节点的最小 key 替换索引节点待删除的 key
             * 2. 如果被删除 key 的叶节点 underflow，再合并两个子节点
             */

            // 删除索引节点
            internalNode.deleteAt(index);

            if (internalNode.getChild(index).isOverflow()) {
                // 因为合并了，所以判断合并后的叶节点是否溢出
                BPlusTreeNode<TKey> node = internalNode.getChild(index).handleOverflow();
                if (node != null) {
                    this.root = node;
                }
            }

            if (internalNode.isUnderflow()) {
                // 因为索引节点删除了一个键，判断索引节点是否不满

                if (internalNode.getParentNode() == null) {
                    // current node is root, only remove keys or delete the whole root node
                    if (internalNode.getKeyCount() == 0) {
                        internalNode.getChild(0).setParentNode(null);
                        this.root = internalNode.getChild(0);
                    }
                } else {
                    BPlusTreeNode<TKey> node = internalNode.handleUnderflow();

                    if (node != null) {
                        this.root = node;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void print() {
        BPlusTreeNode<TKey> node = this.root;

        if (node.getNodeType() == BPlusTreeNodeType.LeafNode) {
            for (int i = 0; i < node.getKeyCount(); i++) {
                System.out.print(node.getKey(i) + " ");
            }
        } else {
            Queue<BPlusTreeInternalNode<TKey>> curlist = new LinkedList<>();
            Queue<BPlusTreeLeafNode<TKey, TValue>> leaflist = new LinkedList<>();

            if (node != null) {
                curlist.add((BPlusTreeInternalNode<TKey>) node);
            }

            while (!curlist.isEmpty()) {
                Queue<BPlusTreeInternalNode<TKey>> next = new LinkedList<>();

                while (!curlist.isEmpty()) {
                    BPlusTreeInternalNode<TKey> curNode = curlist.remove();

                    for (int i = 0; i < curNode.getKeyCount(); i++) {
                        System.out.print(curNode.getKey(i) + " ");
                    }
                    System.out.print("\t");

                    if (curNode.getChild(0).getNodeType() == BPlusTreeNodeType.InternalNode) {
                        for (int i = 0; i < curNode.getKeyCount() + 1; i++) {
                            next.add((BPlusTreeInternalNode<TKey>) curNode.getChild(i));
                        }
                    } else {
                        for (int i = 0; i < curNode.getKeyCount() + 1; i++) {
                            leaflist.add((BPlusTreeLeafNode<TKey, TValue>) curNode.getChild(i));
                        }
                    }
                }
                System.out.println();

                curlist = next;
            }

            for (BPlusTreeLeafNode<TKey, TValue> curNode : leaflist) {
                for (int i = 0; i < curNode.getKeyCount(); i++) {
                    System.out.print(curNode.getKey(i) + " ");
                }

                System.out.print("\t");
            }
        }

        System.out.println();
    }

    public void visualize() {
        visualize("bplustree");
    }

    public void visualize(String pictureName) {
        Writer writer = null;

        String filePath = ".\\test\\" + pictureName;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + ".dot"), "utf-8"));
            writer.write("digraph G {\nnode [shape = record];\n\n");
            write2dot(writer);
            writer.write("}");

            String dot2png = "dot -Tpng " + filePath + ".dot -o " + filePath + ".png";
            Runtime.getRuntime().exec(dot2png);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void write2dot(Writer writer) throws IOException {
        BPlusTreeNode<TKey> node = this.root;
        int nodeID = 0;

        // 横向遍历一遍 B+ 树
        if (node.getNodeType() == BPlusTreeNodeType.LeafNode) {
            writeLeafNode(writer, (BPlusTreeLeafNode<TKey, TValue>) node, nodeID);
            nodeID++;
        } else {
            Queue<BPlusTreeInternalNode<TKey>> curNodelist = new LinkedList<>();
            Queue<BPlusTreeLeafNode<TKey, TValue>> leaflist = new LinkedList<>();

            if (node != null) {
                curNodelist.add((BPlusTreeInternalNode<TKey>) node);
            }

            while (!curNodelist.isEmpty()) {
                Queue<BPlusTreeInternalNode<TKey>> next = new LinkedList<>();

                connectInternalWithLeaf(writer, curNodelist, nodeID);

                int beginID = nodeID;
                while (!curNodelist.isEmpty()) {
                    BPlusTreeInternalNode<TKey> curNode = curNodelist.remove();

                    writeInternalNode(writer, curNode, nodeID);
                    nodeID += 1;

                    if (curNode.getChild(0).getNodeType() == BPlusTreeNodeType.InternalNode) {
                        for (int i = 0; i < curNode.getKeyCount() + 1; i++) {
                            next.add((BPlusTreeInternalNode<TKey>) curNode.getChild(i));
                        }
                    } else {
                        for (int i = 0; i < curNode.getKeyCount() + 1; i++) {
                            leaflist.add((BPlusTreeLeafNode<TKey, TValue>) curNode.getChild(i));
                        }
                    }
                }
                writeSameRank(writer, beginID, nodeID);

                curNodelist = next;
            }

            int beginID = nodeID;
            for (BPlusTreeLeafNode<TKey, TValue> curNode : leaflist) {
                writeLeafNode(writer, curNode, nodeID);
                nodeID += 1;
            }

            writeSameRank(writer, beginID, nodeID);
            connectLeafNode(writer, beginID, nodeID);
            // connectLeafWithValue(writer, leaflist, beginID);
        }
    }

    private void writeInternalNode(Writer writer, BPlusTreeInternalNode<TKey> node, int nodeID) throws IOException {
        // int bound = node.getKeyCount();
        int bound = node.getOrder();

        String keyStr = "{";
        for (int i = 0; i < bound; i++) {
            String port = " <" + "key" + i + ">";
            keyStr += port + node.getKey(i) + " |";
        }
        keyStr = keyStr.substring(0, keyStr.length() - 1) + "}";

        String childStr = "{";
        for (int i = 0; i < bound + 1; i++) {
            String port = " <" + "ptr" + i + ">";
            childStr += port + " |";
        }
        childStr = childStr.substring(0, childStr.length() - 1) + "}";

        String nodeStr = nodeID + " [label=\"{";
        nodeStr += keyStr + "|" + childStr + "}\"]\n";

        writer.write(nodeStr);
    }

    private void writeLeafNode(Writer writer, BPlusTreeLeafNode<TKey, TValue> node, int nodeID) throws IOException {
        // int bound = node.getKeyCount();
        int bound = node.getOrder();

        String keyStr = "";
        for (int i = 0; i < bound; i++) {
            String port = " <" + "key" + i + ">";
            keyStr += port + node.getKey(i) + " |";
        }
        keyStr = keyStr.substring(0, keyStr.length() - 1);

        String nodeStr = nodeID + " [label=\"";
        nodeStr += keyStr + "\"]\n";

        writer.write(nodeStr);
    }

    private void writeSameRank(Writer writer, int beginID, int endID) throws IOException {
        String rankStr = "{rank = same;";

        for (int i = beginID; i < endID; i++) {
            rankStr += " " + i + ";";
        }
        rankStr += "}\n\n";

        writer.write(rankStr);
    }

    private void connectLeafNode(Writer writer, int beginID, int endID) throws IOException {
        for (int i = beginID; i < endID - 1; i++) {
            String connectStr = i + "->" + (i + 1) + " [dir=both];\n";
            writer.write(connectStr);
        }
    }

    @SuppressWarnings("unused")
    private void connectLeafWithValue(Writer writer, Queue<BPlusTreeLeafNode<TKey, TValue>> leaflist, int curNodeID)
            throws IOException {
        for (BPlusTreeLeafNode<TKey, TValue> curNode : leaflist) {
            for (int j = 0; j < curNode.getKeyCount(); j++) {
                String valueNodeName = "\"" + curNode.getKey(j) + "val" + curNode.getValue(j) + "\"";
                String createValueNodeStr = valueNodeName + " [shape=oval, label=" + curNode.getValue(j) + "];\n";
                writer.write(createValueNodeStr);

                String port = "key" + j + ":s";
                String connectStr = curNodeID + ":" + port + "->" + valueNodeName + ";\n";
                writer.write(connectStr);
            }
            curNodeID += 1;
        }
    }

    private void connectInternalWithLeaf(Writer writer, Queue<BPlusTreeInternalNode<TKey>> curNodelist, int curNodeID)
            throws IOException {
        int previousChildrenNum = 0;
        int i = 0;
        for (BPlusTreeInternalNode<TKey> curNode : curNodelist) {
            for (int j = 0; j < curNode.getKeyCount() + 1; j++) {
                String port = "ptr" + j + ":s";
                int childID = curNodeID + curNodelist.size() - i + j + previousChildrenNum;
                String connectStr = curNodeID + ":" + port + "->" + childID + ";\n";
                writer.write(connectStr);
            }

            previousChildrenNum += curNode.getKeyCount() + 1;
            curNodeID += 1;
            i += 1;
        }
    }
}
