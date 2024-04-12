package com.project.nearby.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Getter
@Setter
@Component
public class Root {
    private double version;
    private String generator;

    @Autowired
    private Osm3s osm3s;
    private ArrayList<Element> elements;


    public double getVersion() {
        return version;
    }
}
