package org;

public class RuleConditionElement {
    protected String leftSide;
    protected String operator;
    protected String rightSide;

    public RuleConditionElement(String leftSide, String operator, String rightSide) {
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
    }

    public String getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(String leftSide) {
        this.leftSide = leftSide;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRightSide() {
        return rightSide;
    }

    public void setRightSide(String rightSide) {
        this.rightSide = rightSide;
    }
}
