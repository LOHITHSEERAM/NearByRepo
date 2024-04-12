package com.project.nearby.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class Eateries implements PropertyType {

    Long id;

    GeoNode geoNode;



}
