package org;

public class RuleConditionElement {
    protected String leftSide;
    protected String operator;
    protected double rightSide;

    public RuleConditionElement(String leftSide, String operator, double rightSide) {
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

    public double getRightSide() {
        return rightSide;
    }

    public void setRightSide(double rightSide) {
        this.rightSide = rightSide;
    }
}
