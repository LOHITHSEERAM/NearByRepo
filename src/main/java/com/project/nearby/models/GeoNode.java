package com.project.nearby.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
public class GeoNode {

    private double longitude;
    private double latitude;
}
