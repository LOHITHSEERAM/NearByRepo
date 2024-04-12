package com.project.nearby.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Getter
@Setter
@Component
public class Osm3s {

    private Date timestamp_osm_base;
    private String copyright;
}
