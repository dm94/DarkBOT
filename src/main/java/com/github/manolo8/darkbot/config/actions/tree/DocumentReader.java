package com.github.manolo8.darkbot.config.actions.tree;

import lombok.SneakyThrows;

import javax.swing.text.Document;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;

public class DocumentReader {
    private final Segment BUFFER = new Segment();
    
    private final Document document;
    private int nextIdx;

    @SneakyThrows
    public DocumentReader(String string) {
        this.document = new PlainDocument(new GapContent(string.length()));
        this.document.insertString(0, string, null);
    }

    public DocumentReader(Document document) {
        this.document = document;
    }

    /**
     * Reads a text (arbitrary string) as defined below:
     * Any character is eligible, except for 3 control characters: '(' ',' and ')'.
     * Either of them may be escaped by using \, and \ can be escaped by \\.
     * @return The next word read in full.
     */
    public String readText() {
        for (int i = nextIdx; i < document.getLength(); i++) {
            char ch = charAt(i);
            if (ch == '\\') {
                // Skip the next char, it's been escaped
                i++;
            } else if (ch == '(' || ch == ',' || ch == ')') {
                // Found the end, poll it and return
                String result = substring(nextIdx, i).replaceAll("\\\\(.)", "$1");
                nextIdx = i;
                return result;
            }
        }
        String remaining = substring(nextIdx, document.getLength() - nextIdx);
        nextIdx = document.getLength();
        return remaining;
    }

    public char peekNext() {
        if (nextIdx >= document.getLength()) return '\0';
        return charAt(nextIdx);
    }

    @SneakyThrows
    public Position getPosition() {
        return document.createPosition(nextIdx);
    }

    /**
     * Peek at the next char ignoring whitespace, if it's {@param ch}, poll it and return true
     * @param ch the char to maybe expect next
     * @return true if the next char is ch, and it was skipped, false otherwise
     */
    public boolean pollIf(char ch) {
        for (int i = nextIdx; i < document.getLength(); i++) {
            char currChar = charAt(i);
            if (currChar == ch) {
                nextIdx = i + 1;
                return true;
            } else if (!Character.isWhitespace(currChar)) {
                return false;
            }
        }
        return false;
    }

    public void reset() {
        this.nextIdx = 0;
    }

    @SneakyThrows
    public String substring(int start, int end) {
        return document.getText(start, end - start);
    }

    @SneakyThrows
    public char charAt(int pos) {
        document.getText(pos, 1, BUFFER);
        return BUFFER.charAt(0);
    }


}
