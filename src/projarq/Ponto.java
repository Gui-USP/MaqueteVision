/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author Guilherme Gama
 */
public class Ponto {

    public int x;
    public int y;
    
    boolean inside(){
        return x >= 0 && x < 37 && y >= 0 && y < 37;
    }

    public int dist2(Ponto p) {
        return (int) (Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
    }
    
    Ponto(int a, int b) {
        x = a;
        y = b;
    }

    Ponto(double a, double b) {
        x = (int)Math.round(a);
        y = (int)Math.round(b);
    }

    Ponto(Ponto a) {
        x = a.x;
        y = a.y;
    }
    
    @Override
    public String toString(){
        return x+","+y;
    }
    
    @Override
    public Ponto clone(){
        return new Ponto(this);
    }

    @Override
    public boolean equals(Object o) {
        return o.getClass() == Ponto.class && ((Ponto)o).x == x && ((Ponto)o).y == y;
    }

    Ponto sum(int a) {
        x += a;
        y += a;
        return this;
    }

    Ponto sum(int a, int b) {
        x += a;
        y += b;
        return this;
    }

    Ponto sum(Ponto a) {
        x += a.x;
        y += a.y;
        return this;
    }

    Ponto sub(Ponto a) {
        x -= a.x;
        y -= a.y;
        return this;
    }

    Ponto mult(int a) {
        x *= a;
        y *= a;
        return this;
    }

    Ponto mult(double a) {
        x *= a;
        y *= a;
        return this;
    }

    Ponto mult(int a, int b) {
        x *= a;
        y *= b;
        return this;
    }
    
    public static void main(String args[]) {
        Ponto a = new Ponto(37,1);
        Ponto z = new Ponto(1,1);
        Ponto vx0 = a.clone().sub(z).mult(1/36.0);
        System.out.println(String.valueOf(vx0.x)+" "+String.valueOf(vx0.y));
        /*
        Ponto vx0 = new Point((cent[36][0].x - cent[0][0].x) / 36, (cent[36][0].y - cent[0][0].y) / 36);
        Point vy0 = new Point((cent[0][36].x - cent[0][0].x) / 36, (cent[0][36].y - cent[0][0].y) / 36);
        Point vx1 = new Point(-(cent[0][36].x - cent[36][36].x) / 36, -(cent[0][36].y - cent[36][36].y) / 36);
        Point vy1 = new Point(-(cent[36][0].x - cent[36][36].x) / 36, -(cent[36][0].y - cent[36][36].y) / 36);
        */
    }

    Ponto rot(int rot) {
        for (int i = 0; i < rot; i++) {
            int temp = x;
            x = -y;
            y = temp;
        }
        return this;
    }

    Ponto mindist2(List<Ponto> list) {
        return list.get(IntStream.range(0,list.size()).reduce((i,j) -> dist2(list.get(i)) > dist2(list.get(j)) ? j : i).getAsInt());
    }

    IntStream dist2(List<Ponto> pl) {
        return pl.stream().mapToInt(p->dist2(p));
    }
}
