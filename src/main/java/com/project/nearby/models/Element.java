package com.project.nearby.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class Element{
    private String type;
    private Long id;
    private double lat;
    private double lon;

    @Autowired
    private Tags tags;
}
