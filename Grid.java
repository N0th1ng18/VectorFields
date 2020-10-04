import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.Math; 
import java.awt.Color;
import java.util.ArrayList;

public class Grid {

    int rows, columns;
    float u_width, u_height;
    Unit[] units;

    int screen_width;
    int screen_height;

    ArrayList<Particle> particles;

    //Mouse
    float mousePressure = -10.0f;

    //Vectors
    float vectorScale = 1.0f;
    double life_time = 10.0f;
    float friction = 0.01f;

    //Pressures
    float spreadFactor = 0.8f; // less than 1.0f;
    float diffuse = 1.01f;

    

    public Grid(int rows, int columns, int width, int height){
        this.screen_width = 0;
        this.screen_height = 0;
        
        this.rows = rows;
        this.columns = columns;
        resize(width, height);

        this.units = new Unit[this.rows * this.columns];
        for(int i=0; i < this.rows * this.columns; i++){
            this.units[i] = new Unit();
        }

        particles = new ArrayList<Particle>();

        setVectorField(columns, rows, 1.0f);

    }

    @SuppressWarnings("unused")
    private void setVectorField(float width_X, float height_Y, float scale){
        //Calculate Vector Field
        /*   
                         _____________
                        |             |
                        |____         |
                        |_   \        |
                        | \   \       |
                        |_,|,_,|,_,_,_|
                        
                width_X,Y   -> Width of centered ViewPort
                scale  ch     -> Scales field vectors
        */
        float X,Y;

        Y = -height_Y/2;
        for(int y=rows-1; y >= 0; y--){

            X = -width_X/2;
            for(int x=0; x < columns; x++){

                //Vector Equation
                this.units[x + y * rows].fieldVec.x = (float)(Math.sin(X));
                this.units[x + y * rows].fieldVec.y = (float)(Math.sin(Y));

                //Scale Vector Field and invert Y (Normal Coordinate plane -> unit array coordinate plane)
                this.units[x + y * rows].fieldVec.x = this.units[x + y * rows].fieldVec.x * scale;
                this.units[x + y * rows].fieldVec.y = -this.units[x + y * rows].fieldVec.y * scale;

                X = X + (width_X / columns);
            }
            Y = Y + (height_Y / rows);
        }
    }

    private void resize(int width, int height){
        if(screen_width == width && screen_height == height){
            return;
        }

        this.u_width = (float)(width) / (float)(columns);
        this.u_height = (float)(height) / (float)(rows);
        //this.x_offset = (width - height) / 2;

        System.out.println(u_width + ", " + width);

        screen_width = width;
        screen_height = height;

    }

    public void update(int width, int height, double time, double dt){
        //Resize Grid
        resize(width, height);

        //Create Particles
        if(MouseInput.leftPressed && MouseInput.entered){
            particles.add(new Particle((int)(MouseInput.x), (int)(MouseInput.y), time, life_time));
        }

        //Update Particles
        for(int i=0; i < particles.size(); i++){

            Particle p = particles.get(i);
            //Reset NetForce
            p.resetNetForce();
            
            //Add Force
            Vec2 avg_vec = sampleParticleForceVec(p);
            if(avg_vec != null){
                p.addForceVec(avg_vec.x, avg_vec.y);
            }

            //Friction
            p.addForceVec((-p.vel.x * friction), (-p.vel.y) * friction);

            //Integration
            p.acc.x = p.netForce.x / p.mass;
            p.acc.y = p.netForce.y / p.mass;
            p.vel.x = p.vel.x + p.acc.x;
            p.vel.y = p.vel.y + p.acc.y;
            p.pos.x = p.pos.x + p.vel.x;
            p.pos.y = p.pos.y + p.vel.y;

            //Remove Particles if located off grid or runs out of life duration
            if(p.pos.x < 0 || p.pos.x >= u_width * columns || p.pos.y < 0 || p.pos.y >= u_height * rows || p.spawn_time + p.duration <= time){
                //Remove Particle
                particles.remove(i);

            }
        }


        //Add Pressure
        if(MouseInput.rightPressed && MouseInput.entered){
            int index = getIndex(MouseInput.x, MouseInput.y);
            units[index].prev_pressure += mousePressure;
        }

        //Grid Color based on Pressure
        for(int i = 0; i < rows * columns; i++){
            if(units[i].prev_pressure >= 0){ //Positive Pressure
                units[i].fill_color = new Color(Math.min(units[i].prev_pressure, 1), 0, 0);
            }else{                      //Negative Pressure
                units[i].fill_color = new Color(0, 0, Math.min(Math.abs(units[i].prev_pressure), 1));
            }
        }

        //Step Pressures -> next_center = (prev_center + (spreadFactor / 2.0f) * (prev_left + prev_right - 2.0f * prev_center)) / Diffuse
        for(int j=1; j < rows - 1; j++){
            for(int i=1; i < columns - 1; i++){

                //Top Row
                //Left Column
                //Right Column
                //Bottow Row
                //Middle Section

                float horizontal_pressure = (units[i + j*rows].prev_pressure + (spreadFactor / 2.0f) * (units[(i-1) + j*rows].prev_pressure + units[(i+1) + j*rows].prev_pressure - 2.0f * units[i + j*rows].prev_pressure)) / diffuse;
                float vertical_pressure = (units[i + j*rows].prev_pressure + (spreadFactor / 2.0f) * (units[i + (j-1)*rows].prev_pressure + units[i + (j+1)*rows].prev_pressure - 2.0f * units[i + j*rows].prev_pressure)) / diffuse;
                
                units[i + j*columns].next_pressure = (horizontal_pressure + vertical_pressure) / 2.0f;
            }
        }

        //Apply new pressures
        for(int i = 0; i < rows * columns; i++){
            units[i].prev_pressure = units[i].next_pressure;
        }
        

    }

    public void render(Graphics g, Graphics2D g2, double alpha){

        //Render Grid
        for(int i=0; i < columns; i++){
            for(int j=0; j < rows; j++){

                int topleft_x = i * (int)(u_width);
                int topleft_y = j * (int)(u_height);
                int center_x = topleft_x + (int)(u_width / 2.0f);
                int center_y = topleft_y + (int)(u_height / 2.0f);

                //Draw Unit FillRect 
                g.setColor(units[i+j*columns].fill_color);
                g2.fillRect(topleft_x, topleft_y, (int)(u_width) -1, (int)(u_height) -1);

                //Draw Unit Border
                // g.setColor(units[i+j*columns].border_color);
                // g2.drawRect(topleft_x, topleft_y, (int)(u_width) -1, (int)(u_height) -1);

                //Draw Index
                // g.setColor(Color.BLACK);
                // g2.drawString(""+(i+j*columns), center_x, center_y);

                //Draw Vector
                // g.setColor(new Color(0, 255, 0));
                // g2.drawLine(center_x, center_y, center_x + (int)(units[i+j*columns].fieldVec.x * vectorScale), center_y + (int)(units[i+j*columns].fieldVec.y * vectorScale));
                // g.setColor(new Color(255, 0, 0));
                // g2.fillRect(center_x + (int)(units[i+j*columns].fieldVec.x * vectorScale), center_y + (int)(units[i+j*columns].fieldVec.y * vectorScale), 1, 1);
            
            }
        }

        //Render Particles
        for(int i=0; i < particles.size(); i++){
            particles.get(i).render(g, g2, alpha);
        }
        
    }

    private int getIndex(float posX, float posY){

        int index = -1;

        //Removes particles off the grid
        if(posX < 0 || posX >= u_width * columns || posY < 0 || posY >= u_height * rows){
            return index;
        }

        //Calculate Grid index from particle's pos
        float x = posX / u_width;
        float y = posY / u_height;

        return index = (int)(x) + (int)(y) * columns;

    }
    

    private Vec2 sampleParticleForceVec(Particle p){

        int index = getIndex(p.pos.x, p.pos.y);
        if(index < 0){
            return null;
        }
        
        //Averages center and surrounding 8 units
        Vec2 avg_vec = new Vec2(0.0f, 0.0f);
        float count = 0;
        // //Top Left
        // if((index - columns) - 1 >= 0 && (index - columns) - 1 <= rows * columns - 1){
        //     avg_vec.x += units[(index - columns) - 1].fieldVec.x;
        //     avg_vec.y += units[(index - columns) - 1].fieldVec.y;
        //     count++;
        // }
        // //Top
        // if(index - columns >= 0 && index - columns <= rows * columns - 1){
        //     avg_vec.x += units[index - columns].fieldVec.x;
        //     avg_vec.y += units[index - columns].fieldVec.y;
        //     count++;
        // }
        // //Top Right
        // if((index - columns) + 1 >= 0 && (index - columns) + 1 <= rows * columns - 1){
        //     avg_vec.x += units[(index - columns) + 1].fieldVec.x;
        //     avg_vec.y += units[(index - columns) + 1].fieldVec.y;
        //     count++;
        // }
        // //Left
        // if(index - 1 >= 0 && index - 1 <= rows * columns - 1){
        //     avg_vec.x += units[index - 1].fieldVec.x;
        //     avg_vec.y += units[index - 1].fieldVec.y;
        //     count++;
        // }
        //Middle
        if(index >= 0 && index <= rows * columns - 1){
            avg_vec.x += units[index].fieldVec.x;
            avg_vec.y += units[index].fieldVec.y;
            count++;
        }
        // //Right
        // if(index + 1 >= 0 && index + 1 <= rows * columns - 1){
        //     avg_vec.x += units[index + 1].fieldVec.x;
        //     avg_vec.y += units[index + 1].fieldVec.y;
        //     count++;
        // }
        // //Bottom Left
        // if((index + columns) - 1 >= 0 && (index + columns) - 1 <= rows * columns - 1){
        //     avg_vec.x += units[(index + columns) - 1].fieldVec.x;
        //     avg_vec.y += units[(index + columns) - 1].fieldVec.y;
        //     count++;
        // }
        // //Bottom
        // if(index + columns >= 0 && index + columns <= rows * columns - 1){
        //     avg_vec.x += units[index + columns].fieldVec.x;
        //     avg_vec.y += units[index + columns].fieldVec.y;
        //     count++;
        // }
        // //Bottom Right
        // if((index + columns) + 1 >= 0 && (index + columns) + 1 <= rows * columns - 1){
        //     avg_vec.x += units[(index + columns) + 1].fieldVec.x;
        //     avg_vec.y += units[(index + columns) + 1].fieldVec.y;
        //     count++;
        // }
        //Divide by count
        avg_vec.x = avg_vec.x / count;
        avg_vec.y = avg_vec.y / count;

        return avg_vec;
    }
    
}