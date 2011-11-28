package noVNC.utils;


public class Rect {
	public int x;
	public int y;
	public int w;	// can be negative - relative to x and y
	public int h;	// can be negative - relative to x and y

	public Rect() {
		x = 0;
		y = 0;
		w = 0;
		h = 0;
	}
	public Rect(int _x, int _y, int _w, int _h) {
		x = _x;
		y = _y;
		w = _w;
		h = _h;
	}
	public Rect(Rect _r) {
		x = _r.x;
		y = _r.y;
		w = _r.w;
		h = _r.h;
	}
	
	public Rect(Point topLeft, Point bottomRight) {
		x = topLeft.x;
		y = topLeft.y;
		w = bottomRight.x - topLeft.x + 1;
		h = bottomRight.y - topLeft.y + 1;
	}
	
	public final int x2() {
		return x + w - 1;
	}
	public void setX2(int x2) {
		w = x2 - x + 1;
	}
	public final int y2() {
		return y + h - 1;
	}
	public void setY2(int y2) {
		h = y2 - y + 1;
	}
	
	public Point getTopLeft() {
		return new Point(x, y);
	}
	public Point getBottomRight() {
		return new Point(x + w, y + h);
	}
	
	public void expand(int _w, int _h) {
		w += _w;
		h += _h;
	}
}
