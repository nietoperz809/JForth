package Catalano;

public class FloatPoint {
    public float x;
    public float y;

    public FloatPoint() {
    }

    public FloatPoint(FloatPoint point) {
        this.x = point.x;
        this.y = point.y;
    }

    public FloatPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public FloatPoint(double x, double y) {
        this.x = (float)x;
        this.y = (float)y;
    }

    public FloatPoint(int x, int y) {
        this.x = (float)x;
        this.y = (float)y;
    }

    public FloatPoint(IntPoint point) {
        this.x = (float)point.x;
        this.y = (float)point.y;
    }

    public FloatPoint(DoublePoint point) {
        this.x = (float)point.x;
        this.y = (float)point.y;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void Add(FloatPoint point) {
        this.x += point.x;
        this.y += point.y;
    }

    public FloatPoint Add(FloatPoint point1, FloatPoint point2) {
        FloatPoint result = new FloatPoint(point1);
        result.Add(point2);
        return result;
    }

    public void Add(float value) {
        this.x += value;
        this.y += value;
    }

    public void Subtract(FloatPoint point) {
        this.x -= point.x;
        this.y -= point.y;
    }

    public FloatPoint Subtract(FloatPoint point1, FloatPoint point2) {
        FloatPoint result = new FloatPoint(point1);
        result.Subtract(point2);
        return result;
    }

    public void Subtract(float value) {
        this.x -= value;
        this.y -= value;
    }

    public void Multiply(FloatPoint point) {
        this.x *= point.x;
        this.y *= point.y;
    }

    public FloatPoint Multiply(FloatPoint point1, FloatPoint point2) {
        FloatPoint result = new FloatPoint(point1);
        result.Multiply(point2);
        return result;
    }

    public void Multiply(float value) {
        this.x *= value;
        this.y *= value;
    }

    public void Divide(FloatPoint point) {
        this.x /= point.x;
        this.y /= point.y;
    }

    public FloatPoint Divide(FloatPoint point1, FloatPoint point2) {
        FloatPoint result = new FloatPoint(point1);
        result.Divide(point2);
        return result;
    }

    public void Divide(float value) {
        this.x /= value;
        this.y /= value;
    }

    public float DistanceTo(FloatPoint anotherPoint) {
        float dx = this.x - anotherPoint.x;
        float dy = this.y - anotherPoint.y;
        return (float)Math.sqrt((double)(dx * dx + dy * dy));
    }

    public IntPoint toIntPoint() {
        return new IntPoint(this.x, this.y);
    }

    public DoublePoint toDoublePoint() {
        return new DoublePoint(this.x, this.y);
    }

    public void Swap() {
        float temp = this.x;
        this.x = this.y;
        this.y = temp;
    }

    public boolean equals(Object obj) {
        if (obj.getClass().isAssignableFrom(DoublePoint.class)) {
            FloatPoint point = (FloatPoint)obj;
            if (this.x == point.x && this.y == point.y) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Float.floatToIntBits(this.x);
        hash = 89 * hash + Float.floatToIntBits(this.y);
        return hash;
    }

    public String toString() {
        return "X: " + this.x + " Y: " + this.y;
    }
}
