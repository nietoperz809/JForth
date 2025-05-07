package Catalano;

public class IntPoint {
    public int x;
    public int y;

    public IntPoint() {
    }

    public IntPoint(IntPoint point) {
        this.x = point.x;
        this.y = point.y;
    }

    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public IntPoint(float x, float y) {
        this.x = (int)x;
        this.y = (int)y;
    }

    public IntPoint(double x, double y) {
        this.x = (int)x;
        this.y = (int)y;
    }

    public IntPoint(FloatPoint point) {
        this.x = (int)point.x;
        this.y = (int)point.y;
    }

    public IntPoint(DoublePoint point) {
        this.x = (int)point.x;
        this.y = (int)point.y;
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void Add(IntPoint point) {
        this.x += point.x;
        this.y += point.y;
    }

    public static IntPoint Add(IntPoint point1, IntPoint point2) {
        IntPoint result = new IntPoint(point1);
        result.Add(point2);
        return result;
    }

    public void Add(int value) {
        this.x += value;
        this.y += value;
    }

    public void Subtract(IntPoint point) {
        this.x -= point.x;
        this.y -= point.y;
    }

    public static IntPoint Subtract(IntPoint point1, IntPoint point2) {
        IntPoint result = new IntPoint(point1);
        result.Subtract(point2);
        return result;
    }

    public void Subtract(int value) {
        this.x -= value;
        this.y -= value;
    }

    public void Multiply(IntPoint point) {
        this.x *= point.x;
        this.y *= point.y;
    }

    public static IntPoint Multiply(IntPoint point1, IntPoint point2) {
        IntPoint result = new IntPoint(point1);
        result.Multiply(point2);
        return result;
    }

    public void Multiply(int value) {
        this.x *= value;
        this.y *= value;
    }

    public void Divide(IntPoint point) {
        this.x /= point.x;
        this.y /= point.y;
    }

    public static IntPoint Divide(IntPoint point1, IntPoint point2) {
        IntPoint result = new IntPoint(point1);
        result.Divide(point2);
        return result;
    }

    public void Divide(int value) {
        this.x /= value;
        this.y /= value;
    }

    public float DistanceTo(IntPoint anotherPoint) {
        float dx = (float)(this.x - anotherPoint.x);
        float dy = (float)(this.y - anotherPoint.y);
        return (float)Math.sqrt((double)(dx * dx + dy * dy));
    }

    public FloatPoint toFloatPoint() {
        return new FloatPoint(this.x, this.y);
    }

    public DoublePoint toDoublePoint() {
        return new DoublePoint(this.x, this.y);
    }

    public void Swap() {
        int temp = this.x;
        this.x = this.y;
        this.y = temp;
    }

    public boolean equals(Object obj) {
        if (obj.getClass().isAssignableFrom(IntPoint.class)) {
            IntPoint point = (IntPoint)obj;
            if (this.x == point.x && this.y == point.y) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.x;
        hash = 67 * hash + this.y;
        return hash;
    }

    public String toString() {
        return "X: " + this.x + " Y: " + this.y;
    }
}
