package com.github.manolo8.darkbot.config.actions.tree;

import com.github.manolo8.darkbot.Bot;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.gui.tree.editors.ConditionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.util.Popups;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.tree.TreeModel;
import java.awt.*;

public class ConditionEditorPanel extends JPanel {

    private final JTextField textArea = new ConditionEditor();
    private final DocumentReader reader = new DocumentReader(textArea.getDocument());

    private final TreeNode root = new TreeNode();
    private final ParseTreeModel treeModel = new ParseTreeModel(root);
    private final JTree tree = new ConditionTree(treeModel);

    private Object highlight;

    public ConditionEditorPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(800, 500));

        add(textArea);
        add(new JScrollPane(tree));

        textArea.getDocument().addDocumentListener((GeneralDocumentListener) e -> {
            try {
                reader.reset();
                root.parse(reader);
                treeModel.updateListeners();
                for (int i = 0; i < tree.getRowCount(); i++) {
                    TreeNode node = (TreeNode) tree.getPathForRow(i).getLastPathComponent();
                    if (!node.isSmall()) tree.expandRow(i);
                }
            } catch (SyntaxException ignored) {
                ignored.printStackTrace();
            }
        });

        tree.addTreeSelectionListener(event -> {
            TreeNode node = (TreeNode) event.getPath().getLastPathComponent();
            setHighlight(node);
        });

        textArea.setText("a1(a2(a3()), b2(), c1())");
        ((ConditionEditor) textArea).init();
    }

    @SneakyThrows
    private void setHighlight(TreeNode node) {
        boolean shouldShow = node != null && node.getStart() != null && node.getEnd() != null;
        if (!shouldShow) {
            if (highlight != null) {
                textArea.getHighlighter().removeHighlight(highlight);
                highlight = null;
            }
        } else {
            int start = node.getStart().getOffset(), end = node.getEnd().getOffset();
            if (highlight == null) {
                highlight = textArea.getHighlighter()
                        .addHighlight(start, end, new FocusHighlightPainter(UIUtils.BLUE_HIGHLIGHT));
            } else {
                textArea.getHighlighter().changeHighlight(highlight, start, end);
            }
        }
    }


    public static void main(String[] args) {
        //UIManager.put("Tree.paintLines", true);
        Bot.setupUI();
        Popups.of("Condition editor", new ConditionEditorPanel())
                .border(BorderFactory.createEmptyBorder())
                .showAsync();
    }

    private static class ConditionTree extends JTree {
        public ConditionTree(TreeModel model) {
            super(model);
        }

        @Override
        public String convertValueToText(Object value,
                                         boolean selected,
                                         boolean expanded,
                                         boolean leaf,
                                         int row,
                                         boolean hasFocus) {
            return ((TreeNode) value).getText();
        }

    }


    static class FocusHighlightPainter extends
            DefaultHighlighter.DefaultHighlightPainter {

        FocusHighlightPainter(Color color) {
            super(color);
        }

        /**
         * Paints a portion of a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset &ge; 0
         * @param offs1 the ending model offset &ge; offs1
         * @param bounds the bounding box of the view, which is not
         *               necessarily the region to paint.
         * @param c the editor
         * @param view View painting for
         * @return region in which drawing occurred
         */
        public Shape paintLayer(Graphics g, int offs0, int offs1,
                                Shape bounds, JTextComponent c, View view) {
            g.setColor(getColor());

            if (offs0 == view.getStartOffset() &&
                    offs1 == view.getEndOffset()) {
                // Contained in view, can just use bounds.
                Rectangle alloc;
                if (bounds instanceof Rectangle) {
                    alloc = (Rectangle)bounds;
                }
                else {
                    alloc = bounds.getBounds();
                }
                g.drawRect(alloc.x, alloc.y, alloc.width - 1, alloc.height - 1);
                return alloc;
            }
            else {
                // Should only render part of View.
                try {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                            offs1,Position.Bias.Backward,
                            bounds);
                    Rectangle r = (shape instanceof Rectangle) ?
                            (Rectangle)shape : shape.getBounds();
                    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
                    return r;
                } catch (BadLocationException e) {
                    // can't render
                }
            }
            // Only if exception
            return null;
        }
    }

}
