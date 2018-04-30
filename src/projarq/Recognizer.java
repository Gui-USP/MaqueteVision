/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Guilherme Gama
 */
public class Recognizer {

    private static double diff(Mat in, Scalar b) {
        double diff = 0;
        int w = in.cols();
        int h = in.rows();
        int size = w * h * (int) in.elemSize();
        byte[] data = new byte[size];
        in.get(0, 0, data);
        for (int i = 0; i < size; i += 3) {
            diff += Math.pow((data[i] & 0xFF) - b.val[0], 2) + Math.pow((data[i + 1] & 0xFF) - b.val[1], 2) + Math.pow((data[i + 2] & 0xFF) - b.val[2], 2);
        }
        return diff / size;
    }

    public static int[][] extractColors(List<Scalar> colors, Mat src, Ponto[][] centers) {
        int[][] idtc = new int[37][37];
        for (int x = 0; x < 37; x++) {
            for (int y = 0; y < 37; y++) {
                Ponto p = centers[x][y];
                Rect rect = new Rect(p.x - 3, p.y + 1, 9, 9);/*verdepois*/
                double dist = 200;
                int best = 0;
                for (int w = 0; w < colors.size(); w++) {
                    double d = diff(src.submat(rect), colors.get(w));
                    if (d < dist) {
                        dist = d;
                        best = w;
                    }
                }
                idtc[x][y] = dist < 200 ? best : colors.size();
            }
        }
        return idtc;
    }

    private static boolean match(int[][] idtc, int i, int x, int y, List<List<Ponto>> l) {
        for (List<Ponto> lp : l) {
            boolean ok = false;
            for (Ponto p : lp) {
                p.sum(x,y);
                if (p.inside() && idtc[p.x][p.y] == i) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                return false;
            }
        }
        return !l.isEmpty();
    }
    
    /*
    private void extractWall() {
        Scalar b = new Scalar(255, 0, 0);
        //ve as peças
        walls.clear();
        List<RegCord> regs = m.getRegs();
        for (int i = 0; i < regs.size(); i++) {
            String nome = m.getObj(i).n;
            int classe = nome.contains("janela") || nome.contains("porta") ? 2 : nome.contains("parede") ? 1 : 0;
            if (classe != 1) {
                continue;
            }
            List<List<Ponto>> l = regs.get(i).getReg(0);
            for (int x = 0; x < 37; x++) {
                for (int y = 0; y < 37; y++) {
                    if (match(i, x, y, l)) {
                        walls.add(new Result(i, 0, x, y));
                        Ponto p = centers[x][y];
                        Rect rect = new Rect( p.x - 3, p.y + 1, 9, 9);//verdepois
                        Imgproc.rectangle(src, rect.br(), rect.tl(), b, 2);
                    }
                }
            }
        }
    }
    
    private void extractObjs() {
        Scalar b = new Scalar(255, 0, 0);
        //ve as peças
        resul.clear();
        List<RegCord> regs = m.getRegs();
        for (int i = 0; i < regs.size(); i++) {
            String nome = m.getObj(i).n;
            int classe = nome.contains("janela") || nome.contains("porta") ? 2 : nome.contains("parede") ? 1 : 0;
            if (classe != 1) {
                continue;
            }
            for (int r = 0; r < 4; r++) {
                List<List<Point>> l = regs.get(i).getReg(r);
                for (int x = 0; x < 37; x++) {
                    for (int y = 0; y < 37; y++) {
                        if (match(i, x, y, l)) {
                            int v[] = rotToWall(l, x, y);
                            resul.add(new Result(i, r, x, y));
                            Point p = centers[x][y];
                            Rect rect = new Rect((int) p.x - 3, (int) p.y + 1, 9, 9);//verdepois
                            Imgproc.rectangle(src, rect.br(), rect.tl(), b, 2);
                        }
                    }
                }
            }
        }
    }
    */
}
