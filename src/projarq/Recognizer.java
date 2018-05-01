/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

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
        return !l.isEmpty() && l.stream().noneMatch(lp -> lp.stream().noneMatch(p -> p.inside() && idtc[p.x][p.y] == i));
    }

    private static List<Ponto> fetch(int[][] idtc, int i, List<List<Ponto>> l) {
        List<Ponto> lr = new ArrayList<>();
        l.stream().forEach((lp) -> lr.add(lp.stream().filter(p -> p.inside() && idtc[p.x][p.y] == i).findFirst().get()));
        return lr;
    }

    public static List<Ponto> complete(List<Ponto> l) {
        int s = l.size();
        for (int i = 0; i < s - 1; i++) {
            for (int j = i + 1; j < s; j++) {
                Ponto pi = l.get(i);
                Ponto pj = l.get(j);
                if (pi.x == pj.x) {
                    int dy = pj.y - pi.y;
                    int sy = dy > 0 ? 1 : -1;
                    for (int y = 1; y < dy * sy; y++) {
                        Ponto p = pi.clone().sum(0, y * sy);
                        if (!l.contains(p)) {
                            l.add(p);
                        }
                    }
                } else if (pi.y == pj.y) {
                    int dx = pj.x - pi.x;
                    int sx = dx > 0 ? 1 : -1;
                    for (int x = 1; x < dx * sx; x++) {
                        Ponto p = pi.clone().sum(x * sx, 0);
                        if (!l.contains(p)) {
                            l.add(p);
                        }
                    }
                }
            }
        }
        return l;
    }

    public static List<Result> extractWalls(int[][] idtc, RegCords regs) {
        List<Result> walls = new ArrayList<>();
        for (int i = 0; i < regs.size(); i++) {
            String name = regs.getName(i);
            if (!name.contains("parede")) {
                continue;
            }
            for (int x = 0; x < 37; x++) {
                for (int y = 0; y < 37; y++) {
                    List<List<Ponto>> l = regs.get(i, 0, x, y);
                    if (match(idtc, i, l)) {
                        walls.add(new Result(name, i, 0, complete(fetch(idtc, i, l))));
                    }
                }
            }
        }
        return walls;
    }

    private static int inwallRot(int x, int y, List<Ponto> walls) {
        for (Ponto p : walls) {
            if (p.x == x && p.y == y) {
                if (walls.contains(new Ponto(x + 1, y)) && walls.contains(new Ponto(x - 1, y))) {
                    return walls.stream().noneMatch(pp -> pp.y > y) ? 0 : 2;
                } else if (walls.contains(new Ponto(x, y + 1)) && walls.contains(new Ponto(x, y - 1))) {
                    return walls.stream().noneMatch(pp -> pp.x > x) ? 3 : 1;
                }
            }
        }
        return 0;
    }

    public static List<Result> extractInWall(int[][] idtc, RegCords regs, List<Ponto> walls) {
        List<Result> inwall = new ArrayList<>();
        for (int i = 0; i < regs.size(); i++) {
            String name = regs.getName(i);
            if (!name.contains("janela") && !name.contains("porta")) {
                continue;
            }
            for (int x = 0; x < 37; x++) {
                for (int y = 0; y < 37; y++) {
                    List<List<Ponto>> l = regs.get(i, 0, x, y);
                    if (match(idtc, i, l)) {
                        inwall.add(new Result(name, i, inwallRot(x, y, walls), fetch(idtc, i, l)));
                    }
                }
            }
        }
        return inwall;
    }

    private static int objsRot(List<Ponto> l, int rot, List<Ponto> walls) {
        if (l.size() > 2 || walls.isEmpty()) {
            return rot;
        }
        Ponto ini = l.get(0);
        if (l.size() == 1) {
            Ponto c = ini.mindist2(walls);
            if (c.x == ini.x) {
                return c.y < ini.y ? 0 : 2;
            }
            if (c.y == ini.y) {
                return c.x < ini.x ? 1 : 3;
            }
            return rot;
        }
        return ini.dist2(walls).min().getAsInt() <= l.get(1).dist2(walls).min().getAsInt() ? rot : (rot + 2) % 4;
    }

    public static List<Result> extractObjs(int[][] idtc, RegCords regs, List<Ponto> walls) {
        List<Result> resul = new ArrayList<>();
        for (int i = 1; i < regs.size(); i++) {
            String name = regs.getName(i);
            if (name.contains("janela") || name.contains("porta") || name.contains("parede")) {
                continue;
            }
            for (int r = 0; r < 4; r++) {
                for (int x = 0; x < 37; x++) {
                    for (int y = 0; y < 37; y++) {
                        List<List<Ponto>> l = regs.get(i, r, x, y);
                        if (match(idtc, i, l)) {
                            List<Ponto> lp = fetch(idtc, i, l);
                            resul.add(new Result(name, i, objsRot(lp, r, walls), lp));
                        }
                    }
                }
            }
        }
        return resul;
    }

    public static List<Result> extractAll(int[][] idtc, RegCords regs) {
        List<Result> resul = extractWalls(idtc, regs);
        List<Ponto> wallps = resul.stream().flatMap(w -> w.l.stream()).collect(Collectors.toList());
        resul.addAll(extractInWall(idtc, regs, wallps));
        resul.addAll(extractObjs(idtc, regs, wallps));

        return resul;
    }
}
