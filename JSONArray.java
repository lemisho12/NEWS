package com.server;

import java.util.ArrayList;
import java.util.List;

public class JSONArray {
    private List<Object> elements;
    
    public JSONArray() {
        elements = new ArrayList<>();
    }
    
    public void add(Object element) {
        elements.add(element);
    }
    
    public Object get(int index) {
        return elements.get(index);
    }
    
    public int length() {
        return elements.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(",");
            
            Object element = elements.get(i);
            if (element instanceof String) {
                sb.append("\"").append(escapeString((String) element)).append("\"");
            } else {
                sb.append(element);
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String escapeString(String str) {
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}