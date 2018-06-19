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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Guilherme Gama
 */
public class Recognizer {

    private static Scalar centroid(Mat m) {
        int w = m.cols();
        int h = m.rows();
        int ch = (int) m.elemSize();
        double[] res = new double[ch];
        byte[] data = new byte[w * h * ch];
        m.get(0, 0, data);
        for (int i = 0; i < data.length; i++) {
            res[i % ch] += data[i] & 0xFF;
        }
        for (int i = 0; i < ch; i++) {
            res[i] /= data.length / ch;
        }
        Scalar s = new Scalar(res);
        return s;
    }

    private static double diff(Mat in, Scalar b) {
        double diff = 0;
        int w = in.cols();
        int h = in.rows();
        int size = w * h * 3;
        int[] data = new int[size];
        in.get(0, 0, data);
        for (int pos = 0; pos < w * h; pos++) {
            double d = 0;
            for (int i = 0; i < 3; i++) {
                d += Math.pow(data[pos * 3 + i] - b.val[i], 2);
            }
            diff += Math.sqrt(d);
        }
        return diff / size;
    }

    private static double diff(Scalar a, Scalar b) {
        double diff = 0;
        for (int i = 0; i < 3; i++) {
            diff += Math.pow(a.val[i] - b.val[i], 2);
        }
        return diff;
    }

    public static int[][] extractColorsa(Mat src, Ponto[][] centers, int n) {

        Mat mask = new Mat();
        Imgproc.cvtColor(src, mask, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mask, mask, 100, 255, Imgproc.THRESH_BINARY_INV);
        /*Mat ele = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new  Size(8,8));
        Imgproc.erode(mask, mask, ele);
        Imgproc.dilate(mask, mask, ele);*/
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        centers = PointMap.mapPoints(contours);
        int[][] idtc = new int[37][37];
        List<Scalar> colors = new ArrayList<>();
        for (int y = 0; y < n + 1; y++) {
            Ponto p = centers[y / n][y % n];
            colors.add(centroid(src.submat(p.getRoi())));
        }
        for (int x = 0; x < 37; x++) {
            for (int y = 0; y < 37; y++) {
                Ponto p = centers[x][y];
                Mat sub = src.submat(p.getRoi());
                double dist = 20;
                int best = 0;
                for (int w = 0; w < n + 1; w++) {
                    double d = diff(sub, colors.get(w));
                    if (d < dist) {
                        dist = d;
                        best = w;
                    }
                }
                idtc[x][y] = dist < 20 ? best : n;
            }
        }
        return idtc;
    }

    private static boolean match(int[][] idtc, int i, List<List<Ponto>> l) {
        return !l.isEmpty() && l.stream().noneMatch(lp -> lp.stream().noneMatch(p -> p.inside() && idtc[p.x][p.y] == i));
    }

    private static List<Ponto> fetch(int[][] idtc, int i, List<List<Ponto>> l) {
        List<Ponto> lr = new ArrayList<>();
        l.stream().forEachOrdered((lp) -> lr.add(lp.stream().filter(p -> p.inside() && idtc[p.x][p.y] == i).findFirst().get()));
        return lr;
    }

    public static List<Ponto> complete(List<Ponto> l) {
        int s = l.size();
        List<Ponto> ret = new ArrayList<>();
        ret.addAll(l);
        for (int i = 0; i < s - 1; i++) {
            for (int j = i + 1; j < s; j++) {
                Ponto pi = ret.get(i);
                Ponto pj = ret.get(j);
                if (pi.x == pj.x) {
                    int dy = pj.y - pi.y;
                    int sy = dy > 0 ? 1 : -1;
                    for (int y = 1; y < dy * sy; y++) {
                        Ponto p = pi.clone().sum(0, y * sy);
                        if (!ret.contains(p)) {
                            ret.add(p);
                        }
                    }
                } else if (pi.y == pj.y) {
                    int dx = pj.x - pi.x;
                    int sx = dx > 0 ? 1 : -1;
                    for (int x = 1; x < dx * sx; x++) {
                        Ponto p = pi.clone().sum(x * sx, 0);
                        if (!ret.contains(p)) {
                            ret.add(p);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static List<Result> extractWalls(int[][] idtc, RegCords regs) {
        List<Result> walls = new ArrayList<>();
        for (int i = regs.size() - 1; i >= 0; i--) {
            String name = regs.getName(i);
            if (regs.getClas(i) != 1) {
                continue;
            }
            for (int r = 0; r < 4; r++) {
                for (int x = 0; x < 37; x++) {
                    for (int y = 0; y < 37; y++) {
                        List<List<Ponto>> l = regs.get(i, r, x, y);
                        if ((regs.size(i) == 4 && r == 0) || regs.size(i) == 3) {
                            if (match(idtc, i, l)) {
                                walls.add(new Result(name, i, r, fetch(idtc, i, l)));
                            }
                        }
                    }
                }
            }
        }
        return walls;
    }

    private static List<Ponto> cocomplete(List<Ponto> walls, List<Ponto> wall) {
        List<Ponto> cwall = new ArrayList<>();

        Ponto c = wall.get(0);
        Ponto e1 = wall.get(1);
        Ponto e2 = wall.get(2);
        if (e1.x == c.x) {
            Ponto temp = e1;
            e1 = e2;
            e2 = temp;
        }
        int s = e1.x > c.x ? 1 : -1;
        Ponto f = new Ponto(0, e1.y);
        for (int i = s; i < 9; i += s) {
            f.x = e1.x + i;
            if (!walls.contains(f)) {
                cwall.add(new Ponto(f));
            } else {
                break;
            }
        }
        s = e2.y > c.y ? 1 : -1;
        f.x = e2.x;
        for (int i = s; i < 9; i += s) {
            f.y = e2.y + i;
            if (!walls.contains(f)) {
                cwall.add(new Ponto(f));
            } else {
                break;
            }
        }
        cwall.addAll(complete(wall));
        return cwall;
    }

    private static List<Ponto> completeWalls(List<Result> l) {
        List<Ponto> walls = new ArrayList<>();
        for (Result r : l) {
            if (r.l.size() == 4) {
                walls.addAll(complete(r.l));
            }
        }
        for (Result r : l) {
            if (r.l.size() == 3) {
                walls.addAll(cocomplete(walls, r.l));
            }
        }
        return walls;
    }

    private static int inwallRot(int x, int y, List<Ponto> walls) {
        for (Ponto p : walls) {
            if (p.x == x && p.y == y) {
                if (walls.contains(new Ponto(x + 1, y)) && walls.contains(new Ponto(x - 1, y))) {
                    return 0;//walls.stream().noneMatch(pp -> pp.y > y) ? 0 : 2;
                } else if (walls.contains(new Ponto(x, y + 1)) && walls.contains(new Ponto(x, y - 1))) {
                    return 1;//walls.stream().noneMatch(pp -> pp.x > x) ? 3 : 1;
                }
            }
        }
        return 0;
    }

    public static List<Result> extractInWall(int[][] idtc, RegCords regs, List<Ponto> walls) {
        List<Result> inwall = new ArrayList<>();
        for (int i = 0; i < regs.size(); i++) {
            String name = regs.getName(i);
            if (regs.getClas(i) != 2) {
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
                return c.x < ini.x ? 3 : 1;
            }
            return rot;
        }
        Ponto b = l.get(1);
        Ponto p = new Ponto(0, 0);
        int sx = b.x == ini.x ? 0 : b.x > ini.x ? 1 : -1;
        int sy = b.y == ini.y ? 0 : b.y > ini.y ? 1 : -1;
        for (int i = 0; i < 5; i++) {
            p.x = b.x + i * sx;
            p.y = b.y + i * sy;
            if (walls.contains(p)) {
                l.set(0, b);
                l.set(1, ini);
                return rot + 2;
            }
            p.x = ini.x - i * sx;
            p.y = ini.y - i * sy;
            if (walls.contains(p)) {
                return rot;
            }
        }
        return rot;
    }

    public static List<Result> extractObjs(int[][] idtc, RegCords regs, List<Ponto> walls) {
        List<Result> resul = new ArrayList<>();
        for (int i = 0; i < regs.size(); i++) {
            String name = regs.getName(i);
            if (regs.getClas(i) != 0) {
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

    public static List<Result> extractAll(int[][] idtc, RegCords regs, List<Ponto> pl) {
        List<Result> resul = extractWalls(idtc, regs);
        List<Ponto> wallps = completeWalls(resul);
        pl.addAll(wallps);
        resul.addAll(extractInWall(idtc, regs, wallps));
        resul.addAll(extractObjs(idtc, regs, wallps));

        return resul;
    }

    public static void main(String[] args) {
        List<Ponto> l = new ArrayList<>();
        l.add(new Ponto(0, 0));
        l.add(new Ponto(1, 1));
        Ponto ini = l.get(0);
        Ponto b = l.get(1);
        l.set(0, b);
        l.set(1, ini);
        System.out.println(ini + " " + l.get(0));
    }

    static int[][] createColors() {
        int[][] c = new int[37][37];
        for (int x = 0; x < 37; x++) {
            for (int y = 0; y < 37; y++) {
                c[x][y] = 13;
            }
        }
        // parede 6,6
        c[6][6] = 11;
        c[21][6] = 11;
        c[6][29] = 11;
        c[21][29] = 11;

        // poltrona 8,8,3
        c[8][8] = 0;
        c[9][8] = 0;
        // poltrona 8,19,3
        c[8][19] = 0;
        c[9][19] = 0;

        //lavatorio acessível
        c[22][23] = 1;
        c[23][23] = 1;

        //Chuveiro
        c[24][28] = 2;
        c[24][27] = 2;

        //Porta menor
        c[21][20] = 3;

        //parede em L
        c[28][29] = 4;
        c[32][29] = 4;
        c[32][25] = 4;
        //parede em L
        c[32][21] = 4;
        c[32][17] = 4;
        c[28][17] = 4;

        //cama 8,13,3
        c[8][13] = 5;
        c[10][13] = 5;
        //cama 8,24,3
        c[8][24] = 5;
        c[10][24] = 5;

        //armario 1
        c[19][16] = 6;
        c[20][16] = 6;
        //armario 1
        c[19][24] = 6;
        c[20][24] = 6;

        //Lavatório 1
        c[19][13] = 7;
        c[20][13] = 7;

        //Bacia sanitária acessível
        c[28][28] = 8;
        c[28][27] = 8;

        // janelas
        c[18][29] = 10;
        c[16][29] = 10;
        c[14][29] = 10;
        c[12][29] = 10;

        return c;
    }
}
