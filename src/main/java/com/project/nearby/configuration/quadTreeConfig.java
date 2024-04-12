package com.project.nearby.configuration;

import com.project.nearby.Index.Node;
import com.project.nearby.Index.QuadTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;

@Configuration
public class quadTreeConfig {
    @Autowired
    GeoHashConfig geoHashConfig;

    @Bean
    Node quadroot() {
        return new Node(true, geoHashConfig.bBox(), null, new ArrayList<>(),new Node[4],new Node[6]);
    }
    @Bean
    QuadTree quadTree() {
        return new QuadTree(3,geoHashConfig.bBox(), 6);
    }

}
