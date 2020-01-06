package org;

public class RuleConditionElementDelta extends  RuleConditionElement{
    protected boolean delta;
    protected Pair conditionPair;

    public RuleConditionElementDelta(String leftSide, String operator, double rightSide, boolean delta, Pair conditionPair) {
        super(leftSide,operator,rightSide);
        this.delta = delta;
        this.conditionPair = conditionPair;
    }

    public boolean isDelta() {
        return delta;
    }

    public void setDelta(boolean delta) {
        this.delta = delta;
    }

    public Pair getConditionPair() {
        return conditionPair;
    }

    public void setConditionPair(Pair conditionPair) {
        this.conditionPair = conditionPair;
    }
}
