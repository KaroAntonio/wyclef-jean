package com.example.vickybilbily.wyclef_map;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vickybilbily on 2017-01-21.
 */

public class Stroke {
    public String id;
    public String user_id;
    public List<Coord> path;
    public float weight;
    public String color;
    public String type;
    public String[] tags;
    public long timestamp;

    public class Coord {
        public double lat;
        public double lng;

        public Coord() {

        }

        public Coord(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    public Stroke() {

    }

    public Stroke(PolylineOptions options, String uid) {
        this.user_id = uid;
        this.weight = options.getWidth();
        this.color = "#" + Integer.toHexString(options.getColor()).substring(2);
        this.path = new ArrayList<>();
        for (LatLng l : options.getPoints()) {
            this.path.add(new Coord(l.latitude, l.longitude));
        }
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public PolylineOptions getPolylineOptions() {
        PolylineOptions options = new PolylineOptions()
                .width(this.weight)
                .color(Color.RED);
        //.color(Integer.parseInt(this.color.split("#")[1],16));
        for (Coord c : this.path) {
            options.add(new LatLng(c.lat, c.lng));
        }
        return options;
    }

}
