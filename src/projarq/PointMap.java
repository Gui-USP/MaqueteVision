/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 *
 * @author Guilherme Gama
 */
public class PointMap {

    static Ponto getXY(Ponto p, Pontod vx, Pontod vy, Ponto[][] cent, int zc) {
        Ponto z = cent[zc][zc];
        double x = p.x - z.x;
        double y = p.y - z.y;
        double b = (y * vx.x - x * vx.y) / (vy.y * vx.x - vy.x * vx.y);
        double a = (x - b * vy.x) / vx.x;
        return new Ponto(a + zc, b + zc);
    }

    static Ponto[][] mapPoints(List<MatOfPoint> contours) {
        ArrayList<Ponto> pa = new ArrayList<>();
        Ponto[][] cent = new Ponto[37][37];
        double X = 0, Y = 0;
        int t = 0;
        for (MatOfPoint contour : contours) {
            Moments ms = Imgproc.moments(contour);
            if (ms.m00 < 100) {
                continue;
            }
            double x = ms.m10 / ms.m00;
            double y = ms.m01 / ms.m00;
            pa.add(new Ponto(x, y));
            X += x;
            Y += y;
            t++;
        }
        Ponto meio = new Ponto(X / t, Y / t);
        for (Ponto p : pa) {
            int x = p.x > meio.x ? 36 : 0;
            int y = p.y > meio.y ? 36 : 0;
            if (cent[x][y] == null || meio.dist2(cent[x][y]) < meio.dist2(p)) {
                cent[x][y] = p;
            }
        }
        Pontod vx0 = new Pontod(cent[36][0]).sub(cent[0][0]).mult(1 / 36.0);
        Pontod vy0 = new Pontod(cent[0][36]).sub(cent[0][0]).mult(1 / 36.0);
        Pontod vx1 = new Pontod(cent[0][36]).sub(cent[36][36]).mult(-1 / 36.0);
        Pontod vy1 = new Pontod(cent[36][0]).sub(cent[36][36]).mult(-1 / 36.0);
        for (Ponto p : pa) {
            Ponto xy;
            if (p.dist2(cent[0][0]) < p.dist2(cent[36][36])) {
                xy = getXY(p, vx0, vy0, cent, 0);
            } else {
                xy = getXY(p, vx1, vy1, cent, 36);
            }
            if (!xy.inside()) {
                System.out.println(String.valueOf(xy.x) + " " + String.valueOf(xy.y) + " FORA");
            }
            if (cent[xy.x][xy.y] != null && xy.x != 0 && xy.x != 36 && xy.y != 0 && xy.y != 36) {
                System.out.println(String.valueOf(xy.x) + " " + String.valueOf(xy.y) + " JA TEM ALI");
            }
            cent[xy.x][xy.y] = p;
        }
        return cent;
    }
}
