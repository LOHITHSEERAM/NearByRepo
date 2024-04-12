package com.project.nearby.services;

import com.project.nearby.Index.QuadTree;
import com.project.nearby.geohash.GeoHashAlgo;
import com.project.nearby.models.*;
import com.project.nearby.util.Shapes.BBox;
import com.project.nearby.util.Shapes.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class EateriesService {

   String overpassturbouri="https://overpass-api.de/api/interpreter";

   @Autowired
   GeoHashAlgo geoHashAlgo;

   @Autowired
   QuadTree quadTree;

    Root root;
    public  void getEateries() {

        RestTemplate restTemplate  = new RestTemplate();

        String requestBody = "[out:json][timeout:100];\n" +
                "// gather results\n" +
                "(\n" +
                "  nwr[\"amenity\"=\"restaurants\"](12.8144795046505,77.43988037109376,13.214545199617387,77.87384033203126);\n" +
                "  nwr[\"amenity\"=\"pub\"](12.8144795046505,77.43988037109376,13.214545199617387,77.87384033203126);\n" +
                "  nwr[\"amenity\"=\"restaurant\"](12.8144795046505,77.43988037109376,13.214545199617387,77.87384033203126);\n" +
                "  nwr[\"amenity\"=\"hotel\"](12.8144795046505,77.43988037109376,13.214545199617387,77.87384033203126);\n" +
                "  nwr[\"amenity\"=\"cafe\"](12.8144795046505,77.43988037109376,13.214545199617387,77.87384033203126);\n" +
                "  nwr[\"amenity\"=\"brewery\"](12.8144795046505,77.43988037109376,13.214545199617387,77.87384033203126);\n" +
                ");\n" +
                "// print results\n" +
                "out geom;";
      ResponseEntity<Root> root_response =  restTemplate.postForEntity(overpassturbouri,requestBody, Root.class);
      if(root_response.getStatusCode().is4xxClientError())
          throw new RuntimeException("Something wrong with the given input");
      else if(root_response.getStatusCode().is5xxServerError())
            throw new RuntimeException("Something wrong with the server.Please try again after some time");
      root = root_response.getBody();
    }

    public void loadEateriesInQuadTree() {

        for(int i=0;i<root.getElements().size();i++) {
            Eateries eateries = new Eateries();
            eateries.setId(root.getElements().get(i).getId());
            GeoNode geoNode = new GeoNode(root.getElements().get(i).getLon(),root.getElements().get(i).getLat());
            quadTree.insert(eateries);
        }
    }

    public List<PropertyType> getnearBy(double Longitude, double Latitude) {
        GeoNode geoNode = new GeoNode(Longitude,Latitude);
        List<PropertyType> properties = quadTree.getNearByWithRange(geoNode,new Circle(Latitude,Longitude,500));
        return properties;
    }
}
