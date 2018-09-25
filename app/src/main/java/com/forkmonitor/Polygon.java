package com.forkmonitor;

import java.util.List;

public class Polygon {

    private String name;
    private List<Point> points;
    private boolean visible = false;
    public int r,g,b;

    public Polygon(String name, List<Point> points) {
        this.name = new String(name);
        this.points = points;
        this.r = 0;
        this.g = (int)(Math.random() * 255);
        this.b = (int)(Math.random() * 255);
    }

    public List<Point> getPoints() {
        return points;
    }

    public boolean containsPoint(Point test) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).y > test.y) != (points.get(j).y > test.y) &&
                    (test.x < (points.get(j).x - points.get(i).x) * (test.y - points.get(i).y) / (points.get(j).y-points.get(i).y) + points.get(i).x)) {
                result = !result;
            }
        }
        return result;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public boolean isVisible(){
        return visible;
    }


}