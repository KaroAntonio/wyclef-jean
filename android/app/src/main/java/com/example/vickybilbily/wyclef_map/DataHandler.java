package com.example.vickybilbily.wyclef_map;

import android.content.Context;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by JKJones1 on 2017-01-22.
 */
public class DataHandler {

    GeoQuery geoQuery;
    List<String> strokesRecieved;

    public String saveStroke(Context context, Stroke stroke) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference strokesRef = database.getReference("strokes").push();
            strokesRef.setValue(stroke);

            GeoFire geoFire = new GeoFire(database.getReference("stroke_geo"));

            String strokeId = strokesRef.getKey();
            for (int i = 0; i < stroke.path.size(); i++) {
                geoFire.setLocation(strokeId + '-' + i, new GeoLocation(stroke.path.get(i).lat, stroke.path.get(i).lng));
            }

            return strokeId;
        } else {
            context.startActivity(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                            .build());
            return null;
        }
    }

    public void subscribeToStrokes(double lat, double lng, final StrokeListener listener) {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        GeoFire geoFire = new GeoFire(database.getReference("stroke_geo"));

        geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), 1);
        strokesRecieved = new ArrayList<>();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                final String strokeId = key.substring(0, key.lastIndexOf("-"));
                if (!strokesRecieved.contains(strokeId)) {
                    strokesRecieved.add(strokeId);
                    DatabaseReference strokesRef = database.getReference("strokes/" + strokeId);
                    strokesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Stroke stroke = dataSnapshot.getValue(Stroke.class);
                            stroke.id = strokeId;
                            listener.onStroke(stroke);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    public interface StrokeListener {
        void onStroke(Stroke stroke);
    }

}
