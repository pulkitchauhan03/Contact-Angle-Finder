package com.example.contactanglefinder;

public class Line {
    double slope, intercept;

    public Line() {
        this.slope = 0;
        this.intercept = 0;
    }

    public Line(int m , int c) {
        this.slope = m;
        this.intercept = c;
    }

    public String toString() {
        return ("y = " + this.slope + "x + " + this.intercept);
    }
}
