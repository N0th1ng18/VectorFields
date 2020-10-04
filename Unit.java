import java.awt.Color;

public class Unit {


    public Vec2 fieldVec;
    public float prev_pressure;
    public float next_pressure;
    public Color border_color;
    public Color fill_color;

    public Unit(){
        this.fieldVec = new Vec2(0.0f, 0.0f);
        this.prev_pressure = 0.0f;
        this.next_pressure = 0.0f;
        this.fill_color = new Color(0, 0, 0);
        this.border_color = new Color(0, 0, 255);
    }
}