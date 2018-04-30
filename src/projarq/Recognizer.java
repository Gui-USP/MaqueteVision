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

    private static boolean match(int[][] idtc, int i, List<List<Ponto>> l) {
        return !l.isEmpty() && l.stream().noneMatch(lp -> lp.stream().noneMatch(p -> p.inside() && idtc[p.x][p.y]==i));
    }

    private static List<Ponto> fetch(int[][] idtc, int i, List<List<Ponto>> l) {
        List<Ponto> lr = new ArrayList<>();
        l.stream().forEach((lp) -> lr.add(lp.stream().filter(p -> p.inside()).findFirst().get()));
        return lr;
    }

    public static List<Result> extractWalls(int[][] idtc, RegCords regs) {
        List<Result> walls = new ArrayList<>();
        for (int i = 0; i < regs.size(); i++) {
            if (!regs.l.get(i).n.contains("parede")) {
                continue;
            }
            for (int x = 0; x < 37; x++) {
                for (int y = 0; y < 37; y++) {
                    List<List<Ponto>> l = regs.get(i, 0, x, y);
                    if (match(idtc, i, l)) {
                        walls.add(new Result(i, 0, fetch(idtc, i, l)));
                    }
                }
            }
        }
        return walls;
    }

    public static List<Result> extractInWall(int[][] idtc, RegCords regs) {
        List<Result> inwall = new ArrayList<>();
        for (int i = 0; i < regs.size(); i++) {
            if (!regs.getName(i).contains("janela") && !regs.getName(i).contains("porta")) {
                continue;
            }
            for (int x = 0; x < 37; x++) {
                for (int y = 0; y < 37; y++) {
                    List<List<Ponto>> l = regs.get(i, 0, x, y);
                    if (match(idtc, i, l)) {
                        inwall.add(new Result(i, 0, fetch(idtc, i, l)));
                    }
                }
            }
        }
        return inwall;
    }

    public static List<Result> extractObjs(int[][] idtc, RegCords regs) {
        List<Result> resul = new ArrayList<>();
        for (int i = 1; i < regs.size(); i++) {
            if (regs.getName(i).contains("janela") || regs.getName(i).contains("porta") || regs.getName(i).contains("parede")) {
                continue;
            }
            for (int r = 0; r < 4; r++) {
                for (int x = 0; x < 37; x++) {
                    for (int y = 0; y < 37; y++) {
                        List<List<Ponto>> l = regs.get(i, r, x, y);
                        if (match(idtc, i, l)) {
                            resul.add(new Result(i, r, fetch(idtc, i, l)));
                        }
                    }
                }
            }
        }
        return resul;
    }

    public static void main(String args[]) {
        int[][] idtc = new int[4][4];
        idtc[0][0] = 1;
        int i = 1;
        List<List<Ponto>> l = new ArrayList<>();
        List<Ponto> lp = new ArrayList<>();
        lp.add(new Ponto(0, 0));
        l.add(lp);
        System.out.println(match(idtc, i, l));
    }
}
