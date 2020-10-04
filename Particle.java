import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Color;

public class Particle {

    public Vec2 pos;
    public Vec2 vel;
    public Vec2 acc;

    public Vec2 netForce;
    public float mass;
    public float width;
    public Color color;
    public Vec2 pos_I;

    public double spawn_time;
    public double duration;

    public Particle(float x, float y, double spawn_time, double duration){
        this.pos = new Vec2(x, y);
        this.vel = new Vec2(0.0f, 0.0f);
        this.acc = new Vec2(0.0f, 0.0f);

        this.netForce = new Vec2(0.0f, 0.0f);

        mass = 10.0f;

        width = 10.0f;

        color = new Color(0, 255, 255);
        this.pos_I = new Vec2(0.0f, 0.0f);

        this.spawn_time = spawn_time;
        this.duration = duration;
    }

    public void render(Graphics g, Graphics2D g2, double alpha){
       
        pos_I.x = (float)(pos.x + vel.x * alpha);
        pos_I.y = (float)(pos.y + vel.y * alpha);

        g.setColor(color);
        g2.draw(new Ellipse2D.Double(pos_I.x - width / 2, pos_I.y - width / 2, width, width));

    }

    public void addForce(float forceX, float forceY, float theta){
        theta = (float)Math.toRadians(theta);
		
		netForce.x = (float) (netForce.x + (forceX * Math.sin(theta)));
		netForce.y = (float) (netForce.y + (forceY * Math.cos(theta)));
    }

    public void addForceVec(float forceX, float forceY){
        vel.x = vel.x + forceX;
        vel.y = vel.y + forceY;
    }
    public void addForceVec_Pos(float forceX, float forceY){
        pos.x = pos.x + forceX;
        pos.y = pos.y + forceY;
    }


    public void resetNetForce(){
		netForce.x = 0.0f;
		netForce.y = 0.0f;
	}
}