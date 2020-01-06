package org;

import java.util.*;

public class Matrix {
    protected Map<String, Attribute> attributes=new HashMap<String, Attribute>();
    protected Map<String, Variant> variants=new HashMap<String, Variant>();

    public Matrix(Map<String,Attribute> attributesMap, Map<String,Variant> variantsMap) {
        this.attributes = attributesMap;
        this.variants = variantsMap;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Attribute> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Variant> getVariants() {
        return variants;
    }

    public void setVariants(Map<String, Variant> variants) {
        this.variants = variants;
    }
}