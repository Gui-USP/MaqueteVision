/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

/**
 *
 * @author Guilherme Gama
 */
public class Pontod {

    public double x;
    public double y;

    public double dist2(Pontod p) {
        return (double) (Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
    }

    Pontod(double a, double b) {
        x = a;
        y = b;
    }

    Pontod(Pontod a) {
        x = a.x;
        y = a.y;
    }

    Pontod(Ponto a) {
        x = a.x;
        y = a.y;
    }

    @Override
    public Pontod clone() {
        return new Pontod(this);
    }

    Pontod sum(double a) {
        x += a;
        y += a;
        return this;
    }

    Pontod sum(double a, double b) {
        x += a;
        y += b;
        return this;
    }

    Pontod sum(Pontod a) {
        x += a.x;
        y += a.y;
        return this;
    }

    Pontod sub(Pontod a) {
        x -= a.x;
        y -= a.y;
        return this;
    }

    Pontod sub(Ponto a) {
        x -= a.x;
        y -= a.y;
        return this;
    }

    Pontod mult(double a) {
        x *= a;
        y *= a;
        return this;
    }

    Pontod mult(double a, double b) {
        x *= a;
        y *= b;
        return this;
    }

    public static void main(String args[]) {
        Pontod a = new Pontod(37, 1);
        Pontod z = new Pontod(1, 1);
        Pontod vx0 = a.clone().sub(z).mult(1 / 36.0);
        System.out.println(String.valueOf(vx0.x) + " " + String.valueOf(vx0.y));
        /*
        Pontod vx0 = new Point((cent[36][0].x - cent[0][0].x) / 36, (cent[36][0].y - cent[0][0].y) / 36);
        Point vy0 = new Point((cent[0][36].x - cent[0][0].x) / 36, (cent[0][36].y - cent[0][0].y) / 36);
        Point vx1 = new Point(-(cent[0][36].x - cent[36][36].x) / 36, -(cent[0][36].y - cent[36][36].y) / 36);
        Point vy1 = new Point(-(cent[36][0].x - cent[36][36].x) / 36, -(cent[36][0].y - cent[36][36].y) / 36);
         */
    }
}
