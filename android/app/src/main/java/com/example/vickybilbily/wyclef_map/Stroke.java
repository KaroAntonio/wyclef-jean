package com.example.vickybilbily.wyclef_map;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vickybilbily on 2017-01-21.
 */

public class Stroke {
    private String user_id;
    private Coord[] path;
    private float weight;
    private String color;
    private String type;
    private String[] tags;
    public long timestamp;

    public class Coord{
        private float lat;
        private float lng;

        public Coord(float lat, float lng){
            this.lat = lat;
            this.lng = lng;
        }
    }

    public Stroke(PolylineOptions options, String uid){
        this.user_id = uid;
        this.weight = options.getWidth();
        this.color = "#" + Integer.toHexString(options.getColor()).substring(2);
        List<Coord> coords = new ArrayList<Coord>();
        for (LatLng l : options.getPoints()){
            coords.add(new Coord((float)l.latitude, (float)l.longitude));
        }
        this.path = coords.toArray(new Coord[coords.size()]);
        this.timestamp =  System.currentTimeMillis() / 1000L;
    }

    public PolylineOptions getPolylineOptions(){
        PolylineOptions options = new PolylineOptions()
                .width(this.weight)
                .color(Color.RED);
                //.color(Integer.parseInt(this.color.split("#")[1],16));
        for (Coord c : this.path){
            options.add(new LatLng(c.lat, c.lng));
        }
        return options;
    }

}
