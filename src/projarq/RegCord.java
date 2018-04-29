/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Point;

/**
 *
 * @author Guilherme Gama
 */
class RegCord {

    public List<List<Point>> l = new ArrayList<>();

    public List<List<Point>> getReg(int rot) {
        List<List<Point>> l2 = new ArrayList<>();
        if(rot>=l.size()){
            return l2;
        }
        for (List<Point> lp : l) {
            List<Point> lp2 = new ArrayList<>();
            for (Point p : lp) {
                Point p2 = p.clone();
                for (int i = 0; i < rot; i++) {
                    double temp = p2.x;
                    p2.x = -p2.y;
                    p2.y = temp;
                }
                lp2.add(p2);
            }
            l2.add(lp2);
        }
        return l2;
    }

    RegCord(Obj o) {
        int v[] = Infop.centroM(o);
        int ox = v[0], oy = v[1];
        
        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 14; j++) {
                Info f = o.m[i][j];
                if (f.t == 1 || f.t == 3 || (f.t == 2 && (f.x != 0 || f.y != 0))) {
                    List<Point> a = new ArrayList<>();
                    int sx = f.x > 0 ? 1 : -1;
                    int sy = f.y > 0 ? 1 : -1;
                    for (int x = 0; x <= f.x * sx; x++) {
                        for (int y = 0; y <= f.y * sy; y++) {
                            a.add(new Point(i + x * sx - ox, j + y * sy - oy));
                        }
                    }
                    l.add(a);
                }
            }
        }
        System.out.println(getReg(0));
    }
}
