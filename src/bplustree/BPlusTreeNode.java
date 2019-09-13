package bplustree;

abstract class BPlusTreeNode<TKey extends Comparable<TKey>> {
    // https://www.quora.com/What-does-K-extends-comparable-V-mean-in-Java-in-context-of-making-Binary-Search-Trees

    protected final static int ORDER = 4;
    protected Object[] keys;
    protected int keyCount;
    protected BPlusTreeNode<TKey> parentNode;
    protected BPlusTreeNode<TKey> leftSibling;
    protected BPlusTreeNode<TKey> rightSibling;

    protected BPlusTreeNode() {
        this.keyCount = 0;
        this.parentNode = null;
        this.leftSibling = null;
        this.rightSibling = null;
    }

    public int getOrder() {
        return ORDER;
    }

    public int getKeyCount() {
        return keyCount;
    }

    @SuppressWarnings("unchecked")
    public TKey getKey(int index) {
        return (TKey) this.keys[index];
    }

    public void setKey(int index, TKey key) {
        this.keys[index] = key;
    }

    public BPlusTreeNode<TKey> getParentNode() {
        return parentNode;
    }

    public void setParentNode(BPlusTreeNode<TKey> parentNode) {
        this.parentNode = parentNode;
    }

    public BPlusTreeNode<TKey> getLeftSibling() {
        if (this.leftSibling != null && this.leftSibling.getParentNode() == this.getParentNode()) {
            return this.leftSibling;
        }

        return null;
    }

    public void setLeftSibling(BPlusTreeNode<TKey> leftSibling) {
        this.leftSibling = leftSibling;
    }

    public BPlusTreeNode<TKey> getRightSibling() {
        if (this.rightSibling != null && this.rightSibling.getParentNode() == this.getParentNode()) {
            return this.rightSibling;
        }

        return null;
    }

    public void setRightSibling(BPlusTreeNode<TKey> rightSibling) {
        this.rightSibling = rightSibling;
    }

    protected int bsearch(TKey key) {
        int first = 0;
        int last = this.getKeyCount();

        while (first < last) {
            // 返回 [first, last) 内第一个不小于 key 值的位置
            int mid = first + (last - first) / 2;

            if (this.getKey(mid).compareTo(key) < 0) {
                first = mid + 1;
            } else {
                last = mid;
            }
        }

        return first;
    }

    public abstract BPlusTreeNodeType getNodeType();

    /**
     * Search a key on current node, if found the key then return its position,
     * otherwise return -1 for a leaf node,
     * return the child node index which should contain the key for a internal node.
     */
    public abstract int find(TKey key);

    public abstract String toString();

    /* The codes below are used to support insertion operation */
    public boolean isOverflow() {
        return this.keyCount >= this.keys.length;
    }

    public BPlusTreeNode<TKey> handleOverflow() {
        int midIndex = this.getKeyCount() / 2;
        TKey upKey = this.getKey(midIndex);

        BPlusTreeNode<TKey> newNode = this.split();

        if (this.getParentNode() == null) {
            this.setParentNode(new BPlusTreeInternalNode<TKey>());
        }
        newNode.setParentNode(this.getParentNode());

        // maintain links of sibling nodes
        newNode.setLeftSibling(this);
        newNode.setRightSibling(this.rightSibling);
        if (this.getRightSibling() != null) {
            this.getRightSibling().setLeftSibling(newNode);
        }
        this.setRightSibling(newNode);

        // push up a key to parent internal node
        return this.getParentNode().mergePushUpKey(upKey, this, newNode);
    }

    protected abstract BPlusTreeNode<TKey> split();

    protected abstract BPlusTreeNode<TKey> mergePushUpKey(TKey key, BPlusTreeNode<TKey> leftChild,
            BPlusTreeNode<TKey> rightChild);

    /* The codes below are used to support deletion operation */
    public boolean isUnderflow() {
        return this.getKeyCount() < (this.keys.length / 2);
    }

    public boolean canLendAKey() {
        return this.getKeyCount() > (this.keys.length / 2);
    }

    public BPlusTreeNode<TKey> handleUnderflow() {
        if (this.getParentNode() == null) {
            return null;
        }

        // try to borrow a key from sibling
        BPlusTreeNode<TKey> leftSibling = this.getLeftSibling();
        if (leftSibling != null && leftSibling.canLendAKey()) {
            this.getParentNode().transferChildren(this, leftSibling, leftSibling.getKeyCount() - 1);
            return null;
        }

        BPlusTreeNode<TKey> rightSibling = this.getRightSibling();
        if (rightSibling != null && rightSibling.canLendAKey()) {
            this.getParentNode().transferChildren(this, rightSibling, 0);
            return null;
        }

        // Can not borrow a key from any sibling, then do fusion with sibling
        if (leftSibling != null) {
            return this.getParentNode().fuseChildren(leftSibling, this);
        } else {
            return this.getParentNode().fuseChildren(this, rightSibling);
        }
    }

    protected abstract void transferChildren(BPlusTreeNode<TKey> borrower, BPlusTreeNode<TKey> lender, int borrowIndex);

    protected abstract TKey transferFromSibling(TKey sinkKey, BPlusTreeNode<TKey> sibling, int borrowIndex);

    protected abstract BPlusTreeNode<TKey> fuseChildren(BPlusTreeNode<TKey> leftChild, BPlusTreeNode<TKey> rightChild);

    protected abstract void fuseWithSibling(TKey sinkKey, BPlusTreeNode<TKey> rightSibling);

    /* The codes below are used to support rotation operation */
    protected abstract void rotate(BPlusTreeNode<TKey> leaf);

    protected abstract void rotateToSibling(BPlusTreeNode<TKey> to);

    protected abstract boolean isFull();
}