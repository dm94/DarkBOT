package com.github.manolo8.darkbot.config.actions.tree;

import com.github.manolo8.darkbot.config.actions.SyntaxException;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TreeNode {
    private final TreeNode parent;

    private Position start;
    private String function = "";
    private List<TreeNode> params;
    private Position end;

    private double totalChildren;

    public TreeNode getParent() {
        return parent;
    }

    public Position getStart() {
        return start;
    }

    public String getFunction() {
        return function;
    }

    public List<TreeNode> getParams() {
        return params;
    }

    public Position getEnd() {
        return end;
    }

    public TreeNode() {
        this(null);
    }

    private TreeNode(TreeNode parent) {
        this.parent = parent;
    }

    public void parse(DocumentReader str) {
        start = str.getPosition();
        this.function = str.readText();

        // Standalone leaf node! a raw value with no (), eg the VAL in:
        // string(VAL) or has-effect(VAL, hero())
        if (!str.pollIf('(')) {
            this.params = null;
            this.totalChildren = 0;
        } else {
            this.function = this.function.trim();
            this.params = new ArrayList<>();

            // 0 parameter function
            if (!str.pollIf(')')) {
                while (true) {
                    TreeNode node = new TreeNode(this);
                    node.parse(str);
                    params.add(node);

                    if (str.pollIf(')')) break;
                    if (str.pollIf(',')) continue;
                    throw new SyntaxException(
                            "Expected one of ')' or ',' but found '" + str.peekNext() + "'",
                            str.getPosition().getOffset());
                }
            }
            totalChildren = params.stream().mapToDouble(ch -> ch.totalChildren + 1).sum();
        }
        end = str.getPosition();
    }

    public boolean isSmall() {
        return totalChildren <= 5;
    }

    public String getText() {
        return (isSmall() ? toString() : function + "(...)");
    }

    public String toString() {
        return function.replaceAll("([\\(,)])", "\\$1") +
                (params == null ? "" : params.stream()
                        .map(TreeNode::toString)
                        .collect(Collectors.joining(", ", "(", ")")));
    }

    public static void main(String[] args) {
        List<String> strings = Arrays.asList(
                "a1(a2(a3()), b2(), c1())",
                "a1(a2(a3(asdf)), b2(), c1())",
                "a1(a2(a3(aaaaaa()), b2(), c1())");

        for (String string : strings) {
            try {
                TreeNode treeNode = new TreeNode();
                treeNode.parse(new DocumentReader(string));
                System.out.println("Successfully parsed '" + string + "' into '" + treeNode);
            } catch (SyntaxException e) {
                System.out.println("Failed to parse '" + string + "' at position " + e.getIdx(string));
                System.out.println(e.getMessage());
                System.out.println(string.substring(0, e.getIdx(string)) + " <-- error here");
            }
        }
    }

}
