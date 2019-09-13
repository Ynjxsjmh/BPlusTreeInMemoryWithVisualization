package bplustree;

public class BPlusTreeInternalNode<TKey extends Comparable<TKey>> extends BPlusTreeNode<TKey> {
    protected Object[] children;

    public BPlusTreeInternalNode() {
        this.keys = new Object[ORDER + 1];
        this.children = new Object[ORDER + 2];
    }

    @SuppressWarnings("unchecked")
    public BPlusTreeNode<TKey> getChild(int index) {
        return (BPlusTreeNode<TKey>) this.children[index];
    }

    public void setChild(int index, BPlusTreeNode<TKey> child) {
        this.children[index] = child;
        if (child != null) {
            child.setParentNode(this);
        }
    }

    @Override
    public BPlusTreeNodeType getNodeType() {
        // TODO Auto-generated method stub
        return BPlusTreeNodeType.InternalNode;
    }

    @Override
    public int find(TKey key) {
        // TODO Auto-generated method stub
        int index = this.bsearch(key);

        if (index >= this.getKeyCount()) {
            return index;
        } else if (this.getKey(index).compareTo(key) == 0) {
            // 当 key 恰好为索引值时，key 应该在右孩子的节点上
            return index + 1;
        } else {
            return index;
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "Internal [pointer=*" + ", keys=" + keys + "]";
    }

    /* The codes below are used to support insertion operation */
    private void insertAt(int index, TKey key, BPlusTreeNode<TKey> leftChild, BPlusTreeNode<TKey> rightChild) {
        // move space for the new key
        for (int i = this.getKeyCount() + 1; i > index; i--) {
            this.setChild(i, this.getChild(i - 1));
        }

        for (int i = this.getKeyCount(); i > index; i--) {
            this.setKey(i, this.getKey(i - 1));
        }

        // insert the new key
        this.setKey(index, key);
        this.setChild(index, leftChild);
        this.setChild(index + 1, rightChild);
        this.keyCount += 1;
    }

    /**
     * When splits a internal node, the middle key is kicked out
     * and be pushed to parent node.
     */
    @Override
    protected BPlusTreeNode<TKey> split() {
        // TODO Auto-generated method stub
        int midIndex = this.getKeyCount() / 2;

        BPlusTreeInternalNode<TKey> newNode = new BPlusTreeInternalNode<>();

        for (int i = midIndex + 1; i < this.getKeyCount(); i++) {
            newNode.setKey(i - midIndex - 1, this.getKey(i));
            this.setKey(i, null);
        }

        for (int i = midIndex + 1; i <= this.getKeyCount(); i++) {
            newNode.setChild(i - midIndex - 1, this.getChild(i));
            newNode.getChild(i - midIndex - 1).setParentNode(newNode);
            this.setChild(i, null);
        }

        this.setKey(midIndex, null);
        newNode.keyCount = this.getKeyCount() - midIndex - 1;
        this.keyCount = midIndex;

        return newNode;
    }

    @Override
    protected BPlusTreeNode<TKey> mergePushUpKey(TKey key, BPlusTreeNode<TKey> leftChild,
            BPlusTreeNode<TKey> rightChild) {
        // find the target position of the new key
        int index = this.bsearch(key);

        // insert the new key
        this.insertAt(index, key, leftChild, rightChild);

        // check whether current node need to be split
        if (this.isOverflow()) {
            return this.handleOverflow();
        } else {
            return this.getParentNode() == null ? this : null;
        }
    }

    /* The codes below are used to support delete operation */
    protected void deleteAt(int index) {
        int i = index;
        for (i = index; i < this.getKeyCount() - 1; i++) {
            this.setKey(i, this.getKey(i + 1));
            this.setChild(i + 1, this.getChild(i + 2));
        }

        this.setKey(i, null);
        this.setChild(i + 1, null);
        this.keyCount -= 1;
    }

    @Override
    protected void transferChildren(BPlusTreeNode<TKey> borrower, BPlusTreeNode<TKey> lender, int borrowIndex) {
        // TODO Auto-generated method stub
        int borrowerChildIndex = 0;

        while (borrowerChildIndex < this.getKeyCount() + 1 && this.getChild(borrowerChildIndex) != borrower) {
            borrowerChildIndex += 1;
        }

        if (borrowerChildIndex == 0) {
            // borrow a key from right sibling
            TKey upKey = borrower.transferFromSibling(this.getKey(borrowerChildIndex), lender, borrowIndex);
            this.setKey(borrowerChildIndex, upKey);
        } else {
            // borrow a key from left sibling
            TKey upKey = borrower.transferFromSibling(this.getKey(borrowerChildIndex - 1), lender, borrowIndex);
            this.setKey(borrowerChildIndex - 1, upKey);
        }
    }

    @Override
    protected BPlusTreeNode<TKey> fuseChildren(BPlusTreeNode<TKey> leftChild, BPlusTreeNode<TKey> rightChild) {
        // TODO Auto-generated method stub
        int index = 0;

        while (index < this.getKeyCount() && this.getChild(index) != leftChild) {
            index += 1;
        }
        TKey sinkKey = this.getKey(index);

        // merge two children and the sink key into the left child node
        leftChild.fuseWithSibling(sinkKey, rightChild);

        // remove the sink key, keep the left child and abandon the right child
        this.deleteAt(index);

        // check whether need to propagate borrow or fusion to parent
        if (this.isUnderflow()) {
            if (this.getParentNode() == null) {
                // current node is root, only remove keys or delete the whole root node
                if (this.getKeyCount() == 0) {
                    leftChild.setParentNode(null);
                    return leftChild;
                } else {
                    return null;
                }
            } else {
                return this.handleUnderflow();
            }
        }

        return null;
    }

    @Override
    protected TKey transferFromSibling(TKey sinkKey, BPlusTreeNode<TKey> sibling, int borrowIndex) {
        // TODO Auto-generated method stub
        BPlusTreeInternalNode<TKey> siblingNode = (BPlusTreeInternalNode<TKey>) sibling;

        TKey upKey = null;

        if (borrowIndex == 0) {
            // borrow the first key from right sibling, append it to tail
            int index = this.getKeyCount();
            this.setKey(index, sinkKey);
            this.setChild(index + 1, siblingNode.getChild(borrowIndex));
            this.keyCount += 1;

            upKey = siblingNode.getKey(0);
            siblingNode.deleteAt(borrowIndex);
        } else {
            // borrow the last key from left sibling, insert it to head
            this.insertAt(0, sinkKey, siblingNode.getChild(borrowIndex + 1), this.getChild(0));
            upKey = siblingNode.getKey(borrowIndex);
            siblingNode.deleteAt(borrowIndex);
        }

        return upKey;
    }

    @Override
    protected void fuseWithSibling(TKey sinkKey, BPlusTreeNode<TKey> rightSibling) {
        // TODO Auto-generated method stub
        BPlusTreeInternalNode<TKey> rightSiblingNode = (BPlusTreeInternalNode<TKey>) rightSibling;

        int j = this.getKeyCount();
        this.setKey(j++, sinkKey);

        for (int i = 0; i < rightSiblingNode.getKeyCount(); i++) {
            this.setKey(j + i, rightSiblingNode.getKey(i));
        }

        for (int i = 0; i < rightSiblingNode.getKeyCount() + 1; i++) {
            this.setChild(j + i, rightSiblingNode.getChild(i));
        }

        this.keyCount += 1 + rightSiblingNode.getKeyCount();

        this.setRightSibling(rightSiblingNode.rightSibling);
        if (rightSiblingNode.rightSibling != null) {
            rightSiblingNode.rightSibling.setLeftSibling(this);
        }
    }

    /*
     * A rotation occurs when a leaf page is full,
     * but one of its sibling pages is not full.
     * Rather than splitting the leaf page, we move a record to its sibling,
     * adjusting the indices as necessary.
     * Typically, the left sibling is checked first (if it exists) and then the
     * right sibling.
     */
    protected void rotate(BPlusTreeNode<TKey> leafNode) {
        BPlusTreeNode<TKey> leftSibling = leafNode.getLeftSibling();
        BPlusTreeNode<TKey> rightSibling = leafNode.getRightSibling();

        int index = 0;
        while (index < this.getKeyCount() + 1 && this.getChild(index) != leafNode) {
            index += 1;
        }

        if (leftSibling != null && leftSibling.getParentNode() == this && (!leftSibling.isFull())) {
            // Rotate a key to the left
            leafNode.rotateToSibling(leftSibling);
            this.setKey(index - 1, this.getChild(index).getKey(0));
        } else if (rightSibling != null && rightSibling.getParentNode() == this && (!rightSibling.isFull())) {
            // Rotate a key to the right
            leafNode.rotateToSibling(rightSibling);
            this.setKey(index, this.getChild(index + 1).getKey(0));
        } else {
            return;
        }
    }

    @Override
    protected void rotateToSibling(BPlusTreeNode<TKey> to) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isFull() {
        // TODO Auto-generated method stub
        return this.getKeyCount() == ORDER;
    }
}
