package com.project.nearby.Index;




import com.project.nearby.models.PropertyType;
import com.project.nearby.util.Shapes.BBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


public class Node {

    Node[] childNodes;
    Node parent;
    boolean isLeaf;
    Node[] neighbourNodes;

    BBox bbox;

    List<PropertyType> list;
    public Node(){

    }
    public Node(boolean isLeaf, BBox bbox, Node parent) {
        this.isLeaf = isLeaf;
        this.bbox = bbox;
        this.list = new ArrayList<>();
        this.parent = parent;
    }

    public Node(boolean isLeaf, BBox bbox, Node parent, List<PropertyType> list, Node[] childNodes,Node[] neighbourNodes) {
        this.isLeaf = isLeaf;
        this.bbox = bbox;
        this.list = list;
        this.parent = parent;
        this.neighbourNodes = neighbourNodes;
        this.childNodes = childNodes;
    }
}
