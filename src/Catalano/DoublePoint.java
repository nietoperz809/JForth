package Catalano;

public class DoublePoint {
    public double x;
    public double y;

    public DoublePoint() {
    }

    public DoublePoint(DoublePoint point) {
        this.x = point.x;
        this.y = point.y;
    }

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DoublePoint(float x, float y) {
        this.x = (double)x;
        this.y = (double)y;
    }

    public DoublePoint(int x, int y) {
        this.x = (double)x;
        this.y = (double)y;
    }

    public DoublePoint(IntPoint point) {
        this.x = (double)point.x;
        this.y = (double)point.y;
    }

    public DoublePoint(FloatPoint point) {
        this.x = (double)point.x;
        this.y = (double)point.y;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void Add(DoublePoint point) {
        this.x += point.x;
        this.y += point.y;
    }

    public DoublePoint Add(DoublePoint point1, DoublePoint point2) {
        DoublePoint result = new DoublePoint(point1);
        result.Add(point2);
        return result;
    }

    public void Add(double value) {
        this.x += value;
        this.y += value;
    }

    public void Subtract(DoublePoint point) {
        this.x -= point.x;
        this.y -= point.y;
    }

    public DoublePoint Subtract(DoublePoint point1, DoublePoint point2) {
        DoublePoint result = new DoublePoint(point1);
        result.Subtract(point2);
        return result;
    }

    public void Subtract(double value) {
        this.x -= value;
        this.y -= value;
    }

    public void Multiply(DoublePoint point) {
        this.x *= point.x;
        this.y *= point.y;
    }

    public DoublePoint Multiply(DoublePoint point1, DoublePoint point2) {
        DoublePoint result = new DoublePoint(point1);
        result.Multiply(point2);
        return result;
    }

    public void Multiply(double value) {
        this.x *= value;
        this.y *= value;
    }

    public void Divide(DoublePoint point) {
        this.x /= point.x;
        this.y /= point.y;
    }

    public DoublePoint Divide(DoublePoint point1, DoublePoint point2) {
        DoublePoint result = new DoublePoint(point1);
        result.Divide(point2);
        return result;
    }

    public void Divide(double value) {
        this.x /= value;
        this.y /= value;
    }

    public double DistanceTo(DoublePoint anotherPoint) {
        double dx = this.x - anotherPoint.x;
        double dy = this.y - anotherPoint.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void Swap() {
        double temp = this.x;
        this.x = this.y;
        this.y = temp;
    }

    public IntPoint toIntPoint() {
        return new IntPoint(this.x, this.y);
    }

    public FloatPoint toFloatPoint() {
        return new FloatPoint(this.x, this.y);
    }

    public boolean equals(Object obj) {
        if (obj.getClass().isAssignableFrom(DoublePoint.class)) {
            DoublePoint point = (DoublePoint)obj;
            if (this.x == point.x && this.y == point.y) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 97 * hash + (int)(Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        return hash;
    }

    public String toString() {
        return "X: " + this.x + " Y: " + this.y;
    }
}
