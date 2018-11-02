package sium.nlu.multi;

import java.awt.Point;

public class ObjectPoint {

	private Point point;
	private double duration;
	private String id;
	
	public ObjectPoint(String id, int x, int y, double duration) {
		setPoint(new Point(x, y));
		setDuration(duration);
		setID(id);
	}
	
	public ObjectPoint(String id, int x, int y) {
		setPoint(new Point(x, y));
		setDuration(1.0); // careful with this
		setID(id);
	}
	
	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	public String toString() {
		return getID() + " " + getPoint().toString() + " " + getDuration();
	}
	
}
