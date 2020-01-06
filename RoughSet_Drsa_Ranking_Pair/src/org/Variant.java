package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Variant {
    protected String name;
    protected Map<String,String> valuesMap=new HashMap<String,String>();
    protected Map<String, Attribute> attributesMap=new HashMap<String, Attribute>();

    public Variant(String name) {
        this.name = name;
    }

    public double getIntValue (String attribute, String value) {
        return attributesMap.get(attribute).integerValues.get(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getValuesMap() {
        return valuesMap;
    }

    public void setValuesMap(Map<String, String> valuesMap) {
        this.valuesMap = valuesMap;
    }

    public Map<String, Attribute> getAttributesMap() {
        return attributesMap;
    }

    public void setAttributesMap(Map<String, Attribute> attributesMap) {
        this.attributesMap = attributesMap;
    }
}
