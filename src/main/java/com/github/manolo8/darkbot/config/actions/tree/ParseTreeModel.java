package com.github.manolo8.darkbot.config.actions.tree;

import com.github.manolo8.darkbot.config.ConfigEntity;
import eu.darkbot.api.config.ConfigSetting;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ParseTreeModel implements TreeModel {

    private final TreeNode root;
    private final List<TreeModelListener> listeners = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    private final Map<ConfigSetting<?>, Consumer> changeListeners = new HashMap<>();

    public ParseTreeModel(TreeNode root) {
        this.root = root;
    }

    public void updateListeners() {
        TreeModelEvent event = new TreeModelEvent(this, (TreeNode[]) null, null, null);
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(event);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        List<TreeNode> children = ((TreeNode) parent).getParams();
        //if (index == children.size()) return new TreeNode("+");
        return children.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return isLeaf(parent) ? 0 : ((TreeNode) parent).getParams().size();//+ 1;
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TreeNode) node).getParams() == null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    public void fireNodeChanged(TreeNode node) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> fireNodeChanged(node));
            return;
        }

        TreePath path = getPathFor(node);

        TreeModelEvent event = new TreeModelEvent(this, path);

        if (path.getParentPath() != null) {
            TreePath parentPath = path.getParentPath();
            int idx = getIndexOfChild(node.getParent(), node);
            // Modified child is not even in the tree! maybe not visible atm?
            if (idx == -1) return;
            event = new TreeModelEvent(this, parentPath, new int[]{idx}, new Object[]{node});
        }
        for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(event);
        }
        ConfigEntity.changed();
    }

    private TreePath getPathFor(TreeNode node) {
        TreeNode parent = node;
        List<TreeNode> path = new ArrayList<>();
        do {
            path.add(parent);
        } while (parent != root && (parent = parent.getParent()) != null);
        Collections.reverse(path);
        return new TreePath(path.toArray(new TreeNode[0]));
    }

    @Override
    public int getIndexOfChild(Object parentObj, Object child) {
        return ((TreeNode) parentObj).getParams().indexOf((TreeNode) child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void clearListeners() {
        listeners.clear();
    }
}

