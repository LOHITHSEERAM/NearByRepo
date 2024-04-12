package com.project.nearby.configuration;

import com.project.nearby.Index.Node;
import com.project.nearby.Index.QuadTree;
import com.project.nearby.geohash.GeoHashAlgo;
import com.project.nearby.geohash.SpatialKeyAlgo;
import com.project.nearby.util.Shapes.BBox;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;

@Configuration
public class GeoHashConfig {

    @Bean
    BBox bBox() {
        return new BBox(-180,180,-90,90);
    }

    @Bean
    SpatialKeyAlgo spatialKeyAlgo() {
        return new SpatialKeyAlgo(16,bBox());
    }

}
