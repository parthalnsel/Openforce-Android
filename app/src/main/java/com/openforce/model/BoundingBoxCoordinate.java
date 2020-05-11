package com.openforce.model;


import com.google.android.gms.maps.model.LatLng;

public class BoundingBoxCoordinate {

    private LatLng topRight;
    private LatLng topLeft;
    private LatLng bottomLeft;
    private LatLng bottomRight;

    public BoundingBoxCoordinate(LatLng topRight, LatLng topLeft, LatLng bottomLeft, LatLng bottomRight) {
        this.topRight = topRight;
        this.topLeft = topLeft;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public LatLng getTopRight() {
        return topRight;
    }

    public void setTopRight(LatLng topRight) {
        this.topRight = topRight;
    }

    public LatLng getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(LatLng topLeft) {
        this.topLeft = topLeft;
    }

    public LatLng getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(LatLng bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public LatLng getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(LatLng bottomRight) {
        this.bottomRight = bottomRight;
    }
}
