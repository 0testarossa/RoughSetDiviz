package org;

public class RuleDecisionElement {
    protected String operator;
    protected String decisionClass;

    public RuleDecisionElement (String operator, String decisionClass) {
        this.operator = operator;
        this.decisionClass = decisionClass;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDecisionClass() {
        return decisionClass;
    }

    public void setDecisionClass(String decisionClass) {
        this.decisionClass = decisionClass;
    }
}
