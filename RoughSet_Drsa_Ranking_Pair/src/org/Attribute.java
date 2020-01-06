package org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Attribute {
    protected String name;
    protected String type;
    protected ArrayList<String> originalValues = new ArrayList<String>();
    protected Map<String,Double> integerValues=new HashMap<String,Double>();
    protected Map<Double,String> stringValues=new HashMap<Double, String>();
    protected String directionPreference;

    public Attribute (String name, String type, ArrayList<String> originalValues) {
        this.name = name;
        this.type=type;
        this.originalValues = originalValues;
        Map<String,Double> integerValues=new HashMap<String,Double>();
        Map<Double,String> stringValues=new HashMap<Double, String>();

        if(type.equals("ordinal")) {
            int i = 0;
            for(String value: originalValues) {
                integerValues.put(value,Double.valueOf(i));
                stringValues.put(Double.valueOf(i++),value);
            }
        } else if (type.equals("numerical")){
            for(String value: originalValues) {
                integerValues.put(value,Double.parseDouble(value));
                stringValues.put(Double.parseDouble(value), value);
            }
        }
        this.integerValues = integerValues;
        this.stringValues = stringValues;
    }

    public Attribute (String name) {
        this.name = name;
    }

    public void computeValues () {
        Map<String,Double> integerValues=new HashMap<String,Double>();
        Map<Double,String> stringValues=new HashMap<Double, String>();

        if(type.equals("ordinal")) {
            int i = 0;
            for(String value: originalValues) {
                integerValues.put(value,Double.valueOf(i));
                stringValues.put(Double.valueOf(i++),value);
            }
        } else if (type.equals("numerical")){
            for(String value: originalValues) {
                integerValues.put(value,Double.parseDouble(value));
                stringValues.put(Double.parseDouble(value), value);
            }
        }
        this.integerValues = integerValues;
        this.stringValues = stringValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getOriginalValues() {
        return originalValues;
    }

    public void setOriginalValues(ArrayList<String> originalValues) {
        this.originalValues = originalValues;
    }

    public Map<String, Double> getIntegerValues() {
        return integerValues;
    }

    public void setIntegerValues(Map<String, Double> integerValues) {
        this.integerValues = integerValues;
    }

    public Map<Double, String> getStringValues() {
        return stringValues;
    }

    public void setStringValues(Map<Double, String> stringValues) {
        this.stringValues = stringValues;
    }

    public String getDirectionPreference() {
        return directionPreference;
    }

    public void setDirectionPreference(String directionPreference) {
        this.directionPreference = directionPreference;
    }
}
