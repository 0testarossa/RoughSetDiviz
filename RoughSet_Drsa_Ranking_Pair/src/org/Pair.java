package org;

public class Pair {
    protected String elementLeft;
    protected String elementRight;

    public Pair (String elementLeft, String elementRight) {
        this.elementLeft = elementLeft;
        this.elementRight = elementRight;
    }

    public String getElementLeft() {
        return elementLeft;
    }

    public void setElementLeft(String elementLeft) {
        this.elementLeft = elementLeft;
    }

    public String getElementRight() {
        return elementRight;
    }

    public void setElementRight(String elementRight) {
        this.elementRight = elementRight;
    }
}
