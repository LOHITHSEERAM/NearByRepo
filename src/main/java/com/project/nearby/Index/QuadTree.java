package com.project.nearby.Index;



import com.project.nearby.models.GeoNode;
import com.project.nearby.models.PropertyType;
import com.project.nearby.util.Shapes.BBox;
import com.project.nearby.util.Shapes.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;



public class QuadTree {


    int capacity;

    int parts;

    double deltaX;

    double deltaY;

    @Autowired
    @Qualifier("quadroot")
    Node quadroot;
    public QuadTree(int capacity, BBox bbox, int parts) {

        this.capacity = capacity;
        deltaY = (bbox.maxLat - bbox.minLat) / parts;
        deltaX = (bbox.maxLon - bbox.minLon) / parts;
    }

    public void insert(Node node, PropertyType propertyType) {
        if (node.isLeaf) {
            if (node.list.size() < capacity)
                node.list.add(propertyType);
            else {
                node.isLeaf = false;
                Node child = getNode(node,propertyType.getGeoNode());
                child.list.add(propertyType);
                for (int i = 0; i < node.list.size(); i++) {
                    Node c = getNode(node, node.list.get(i).getGeoNode());
                    c.list.add(node.list.get(i));
                }
            }
        } else {
            Node child = getNode(node, propertyType.getGeoNode());
            insert(child, propertyType);
        }
    }

    public Node getNode(Node parent, GeoNode geoNode) {

        double latMid = (parent.bbox.minLat + parent.bbox.maxLat) / 2;
        double lonMid = (parent.bbox.maxLon + parent.bbox.minLon) / 2;


        if (geoNode.getLongitude() >= lonMid && geoNode.getLatitude() >= latMid) {

            if (parent.childNodes[0] == null)
                parent.childNodes[0] = new Node(true, new BBox(lonMid, parent.bbox.maxLon, latMid, parent.bbox.maxLat), parent);

            return parent.childNodes[0];
        } else if (geoNode.getLongitude() >= lonMid && geoNode.getLatitude() < latMid) {

            if (parent.childNodes[2] == null)
                parent.childNodes[2] = new Node(true, new BBox(lonMid, parent.bbox.maxLon, parent.bbox.minLat, latMid), parent);

            return parent.childNodes[2];
        } else if (geoNode.getLongitude() < lonMid && geoNode.getLatitude() >= latMid) {

            if (parent.childNodes[1] == null)
                parent.childNodes[1] = new Node(true, new BBox(parent.bbox.minLon, lonMid, latMid, parent.bbox.maxLat), parent);

            return parent.childNodes[1];
        } else {

            if (parent.childNodes[3] == null)
                parent.childNodes[3] = new Node(true, new BBox(parent.bbox.minLon, lonMid, parent.bbox.minLat, latMid), parent);

            return parent.childNodes[3];
        }
    }

    public void insert(PropertyType propertyType) {

        insert(quadroot, propertyType);
    }

    public List<PropertyType> getNearByWithoutRange(Node node, GeoNode geoNode) {

        if (node.isLeaf)
            return node.list;

        return getNearByWithoutRange(getNode(node, geoNode), geoNode);
    }

    public List<PropertyType> getNearByWithRange(GeoNode geoNode, Circle s) {
        ArrayList<PropertyType> nearByList = new ArrayList<>();
        Deque<Node> q = new ArrayDeque<>();
        BBox CircleBbox = s.bbox;
        q.addLast(quadroot);

        while (!q.isEmpty()) {

            Node node = q.pollFirst();
            if (node.isLeaf) {
                for (int j = 0; j < node.list.size(); j++) {
                    if (s.contains(node.list.get(j).getGeoNode().getLatitude(), node.list.get(j).getGeoNode().getLongitude()))
                        nearByList.add(node.list.get(j));
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    if (node.childNodes[i] != null) {
                        if (node.childNodes[i].bbox.contains(CircleBbox)) {
                            q.addLast(node.childNodes[i]);
                            break;
                        } else if (s.contains(node.childNodes[i].bbox) || s.intersects(node.childNodes[i].bbox)) {
                            q.addLast(node.childNodes[i]);
                        }
                    }
                }
            }
        }
        return nearByList;
    }

    public List<PropertyType> getNearByWithoutRange(GeoNode geoNode) {
        return getNearByWithoutRange(quadroot, geoNode);
    }
}
