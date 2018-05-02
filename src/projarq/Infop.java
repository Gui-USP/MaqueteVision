/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.util.List;
import org.opencv.core.Point;

/**
 *
 * @author Guilherme Gama
 */
public class Infop {

    public static void set(Info p, int t, int x, int y) {
        p.t = t;
        p.x = x;
        p.y = y;
    }

    public static void setM(Info[][] mat, int x0, int y0, int xd, int yd, int t, int x, int y) {
        int sx = xd > 0 ? 1 : -1;
        int sy = yd > 0 ? 1 : -1;
        for (int i = 0; i <= xd * sx; i++) {
            for (int j = 0; j <= yd * sy; j++) {
                set(mat[x0 + i * sx][y0 + j * sy], t, x, y);
            }
        }
    }

    public static boolean emptyM(Info[][] mat, int s) {
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                if (mat[i][j].t != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean wrongM(Info[][] mat, int s) {
        int q = 0;
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < s; j++) {
                if (mat[i][j].t == 3) {
                    q++;
                }
            }
        }
        return q != 1;
    }

    static int[] centroM(Obj o) {
        int v[] = new int[2];
        for (int pos = 0; pos < 14 * 14; pos++) {
            Info f = o.m[pos / 14][pos % 14];
            if (f.t == 3) {
                v[0] = pos / 14;
                v[1] = pos % 14;
                break;
            }
        }
        return v;
    }
}
