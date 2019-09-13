package bplustree;
public class BPlusTreeLeafNode<TKey extends Comparable<TKey>, TValue> extends BPlusTreeNode<TKey> {
    private Object[] values;

    public BPlusTreeLeafNode() {
        this.keys = new Object[ORDER + 1];
        this.values = new Object[ORDER + 1];
    }

    @SuppressWarnings("unchecked")
    public TValue getValue(int index) {
        return (TValue) this.values[index];
    }

    public void setValue(int index, TValue value) {
        this.values[index] = value;
    }

    @Override
    public BPlusTreeNodeType getNodeType() {
        // TODO Auto-generated method stub
        return BPlusTreeNodeType.LeafNode;
    }

    @Override
    public int find(TKey key) {
        // TODO Auto-generated method stub
        int index = this.bsearch(key);

        if (this.getKey(index).compareTo(key) == 0) {
            return index;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "Leaf [key=" + keys + ", values=" + values + "]";
    }

    public void insert(TKey key, TValue value) {
        int index = this.bsearch(key);

        for (int i = this.getKeyCount() - 1; i >= index; i--) {
            // move space for the new key
            this.setKey(i + 1, this.getKey(i));
            this.setValue(i + 1, this.getValue(i));
        }

        // insert new key and value
        this.setKey(index, key);
        this.setValue(index, value);
        this.keyCount += 1;
    }

    /**
     * When splits a leaf node, the middle key is kept on new node
     * and be pushed to parent node.
     */
    @Override
    protected BPlusTreeNode<TKey> split() {
        // 分裂当前叶节点
        int midIndex = this.getKeyCount() / 2;

        BPlusTreeLeafNode<TKey, TValue> newNode = new BPlusTreeLeafNode<>();

        for (int i = midIndex; i < this.getKeyCount(); i++) {
            newNode.setKey(i - midIndex, this.getKey(i));
            newNode.setValue(i - midIndex, this.getValue(i));
            this.setKey(i, null);
            this.setValue(i, null);
        }

        newNode.keyCount = this.getKeyCount() - midIndex;
        this.keyCount = midIndex;

        return newNode;
    }

    @Override
    protected BPlusTreeNode<TKey> mergePushUpKey(TKey key, BPlusTreeNode<TKey> leftChild,
            BPlusTreeNode<TKey> rightNode) {
        throw new UnsupportedOperationException();
    }

    /* The codes below are used to support deletion operation */
    public boolean delete(TKey key) {
        int index = this.find(key);

        if (index == -1) {
            return false;
        }

        this.deleteAt(index);

        return true;
    }

    private void deleteAt(int index) {
        int i = index;
        for (i = index; i < this.getKeyCount() - 1; i++) {
            this.setKey(i, this.getKey(i + 1));
            this.setValue(i, this.getValue(i + 1));
        }

        this.setKey(i, null);
        this.setValue(i, null);
        this.keyCount -= 1;
    }

    @Override
    protected void transferChildren(BPlusTreeNode<TKey> borrower, BPlusTreeNode<TKey> lender, int borrowIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected BPlusTreeNode<TKey> fuseChildren(BPlusTreeNode<TKey> leftChild, BPlusTreeNode<TKey> rightChild) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TKey transferFromSibling(TKey sinkKey, BPlusTreeNode<TKey> sibling, int borrowIndex) {
        // TODO Auto-generated method stub
        BPlusTreeLeafNode<TKey, TValue> siblingNode = (BPlusTreeLeafNode<TKey, TValue>) sibling;

        this.insert(siblingNode.getKey(borrowIndex), siblingNode.getValue(borrowIndex));
        siblingNode.deleteAt(borrowIndex);

        return borrowIndex == 0 ? siblingNode.getKey(0) : this.getKey(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void fuseWithSibling(TKey sinkKey, BPlusTreeNode<TKey> rightSibling) {
        // 因为叶节点中肯定包含 sinkKey，所以不用再在叶节点中添加
        // TODO Auto-generated method stub
        BPlusTreeLeafNode<TKey, TValue> siblingLeaf = (BPlusTreeLeafNode<TKey, TValue>) rightSibling;

        int j = this.getKeyCount();

        for (int i = 0; i < siblingLeaf.getKeyCount(); i++) {
            this.setKey(j + i, siblingLeaf.getKey(i));
            this.setValue(j + i, siblingLeaf.getValue(i));
        }
        this.keyCount += siblingLeaf.getKeyCount();

        this.setRightSibling(siblingLeaf.rightSibling);
        if (siblingLeaf.rightSibling != null) {
            siblingLeaf.rightSibling.setLeftSibling(this);
        }
    }

    @Override
    protected void rotate(BPlusTreeNode<TKey> leaf) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void rotateToSibling(BPlusTreeNode<TKey> to) {
        // TODO Auto-generated method stub
        BPlusTreeLeafNode<TKey, TValue> target = (BPlusTreeLeafNode<TKey, TValue>) to;

        if (this.getKey(0).compareTo(target.getKey(0)) > 0) {
            // 移到左节点
            target.insert(this.getKey(0), this.getValue(0));
            this.deleteAt(0);
        } else {
            // 移到右节点
            target.insert(this.getKey(this.keyCount - 1), this.getValue(this.keyCount - 1));
            this.deleteAt(this.keyCount - 1);
        }
    }

    @Override
    protected boolean isFull() {
        // TODO Auto-generated method stub
        return this.getKeyCount() == ORDER;
    }
}
