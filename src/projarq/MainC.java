/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import static org.opencv.core.CvType.CV_8UC3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

/**
 *
 * @author Guilherme Gama
 */
public class MainC implements Observer {

    MainW v;
    Model m;
    CalibC cc;
    EditC ec;
    BufferedImage ferr;
    Mat src;
    Point meio;
    Point[][] centers = new Point[37][37];
    int[][] idtc = new int[37][37];
    List<Result> resul = new ArrayList<>();

    BufferedImage im2;
    Graphics2D gp;

    MainC() {
        ferr = mat2BufferedImg(new Mat(new Size(220, 220), CV_8UC3, new Scalar(0, 255, 0)));
        im2 = new BufferedImage(36 * 30 + 40, 36 * 30 + 40, BufferedImage.TYPE_3BYTE_BGR);
        gp = im2.createGraphics();
    }

    public void init() {
        m = new Model();
        m.addObserver(this);
        v = new MainW(this, ferr);
        v.setVisible(true);
        v.getFromWebBtn().setEnabled(false);
        setListeners();
        m.load();
        m.startThread();
        v.startThread();
    }

    public List<Point> getCalibPoints(int n) {
        List<Point> p = new ArrayList<>();
        if (centers[0][0] == null) {
            return null;
        }
        for (int i = 0; i < n; i++) {
            p.add(centers[0][i]);
        }
        return p;
    }

    public void capture() {
        imwrite("src/projarq/cap.bmp", src);
    }

    public void closedMain() {
        m.release();
        System.exit(0);
    }

    void openCalib() {
        v.getCalibBtn().setEnabled(false);
        cc = new CalibC(this, m);
    }

    void openEdit() {
        v.getEditBtn().setEnabled(false);
        ec = new EditC(this, m);
    }

    public void nextCam() {
        v.getNextBtn().setEnabled(false);
        v.getCamNum().setText("Webcam: " + m.nextCam());
        v.getNextBtn().setEnabled(true);
    }

    public void closedCalib() {
        v.getCalibBtn().setEnabled(true);
    }

    public void closedEdit() {
        v.getEditBtn().setEnabled(true);
    }

    private void setListeners() {
        v.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closedMain();
            }
        });
        v.getNextBtn().addActionListener((ActionEvent ae) -> {
            nextCam();
        });
        v.getCapBtn().addActionListener((ActionEvent ae) -> {
            capture();
        });
        v.getCalibBtn().addActionListener((ActionEvent ae) -> {
            openCalib();
        });
        v.getEditBtn().addActionListener((ActionEvent ae) -> {
            openEdit();
        });
        v.getFromWebBtn().addActionListener((ActionEvent ae) -> {
            v.getFromWebBtn().setEnabled(false);
            v.getFromArqBtn().setEnabled(true);
            m.fromWeb = true;
        });
        v.getFromArqBtn().addActionListener((ActionEvent ae) -> {
            v.getFromWebBtn().setEnabled(true);
            v.getFromArqBtn().setEnabled(false);
            m.fromWeb = false;
        });
    }

    public static int dist2(Point p1, Point p2) {
        return (int) (Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    Point getXY(Point p, Point vx, Point vy, Point[][] cent, int zc) {
        Point z = cent[zc][zc];
        double x = p.x - z.x;
        double y = p.y - z.y;
        double b = (y * vx.x - x * vx.y) / (vy.y * vx.x - vy.x * vx.y);
        double a = (x - b * vy.x) / vx.x;
        return new Point(a + zc, b + zc);
    }

    void mapPoints(List<MatOfPoint> contours) {
        ArrayList<Point> pa = new ArrayList<>();
        Point[][] cent = new Point[37][37];
        double X = 0, Y = 0;
        int t = 0;
        for (MatOfPoint contour : contours) {
            Moments ms = Imgproc.moments(contour);
            double x = ms.m10 / ms.m00;
            double y = ms.m01 / ms.m00;
            pa.add(new Point(x, y));
            X += x;
            Y += y;
            t++;
        }
        meio = new Point(X / t, Y / t);
        for (Point p : pa) {
            int x = p.x > meio.x ? 36 : 0;
            int y = p.y > meio.y ? 36 : 0;
            if (cent[x][y] == null || dist2(meio, cent[x][y]) < dist2(meio, p)) {
                cent[x][y] = p;
            }
        }
        Point vx0 = new Point((cent[36][0].x - cent[0][0].x) / 36, (cent[36][0].y - cent[0][0].y) / 36);
        Point vy0 = new Point((cent[0][36].x - cent[0][0].x) / 36, (cent[0][36].y - cent[0][0].y) / 36);
        Point vx1 = new Point(-(cent[0][36].x - cent[36][36].x) / 36, -(cent[0][36].y - cent[36][36].y) / 36);
        Point vy1 = new Point(-(cent[36][0].x - cent[36][36].x) / 36, -(cent[36][0].y - cent[36][36].y) / 36);
        for (Point p : pa) {
            Point xy;
            if (dist2(p, cent[0][0]) < dist2(p, cent[36][36])) {
                xy = getXY(p, vx0, vy0, cent, 0);
            } else {
                xy = getXY(p, vx1, vy1, cent, 36);
            }
            int x = (int) Math.round(xy.x);
            int y = (int) Math.round(xy.y);
            if (x > 36 || y > 36 || x < 0 || y < 0) {
                System.out.println(String.valueOf(x) + " " + String.valueOf(y) + " FORA");
            }
            if (cent[x][y] != null && x != 0 && x != 36 && y != 0 && y != 36) {
                System.out.println(String.valueOf(x) + " " + String.valueOf(y) + " JA TEM ALI");
            }
            cent[x][y] = p;
        }
        centers = cent;
    }

    private Scalar centroid(Mat m) {
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
            res[i] /= data.length / 3;
        }
        Scalar s = new Scalar(res);
        return s;
    }

    private double diff(Mat in, Scalar b) {
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

    private boolean match(int i, int x, int y, List<List<Point>> l) {
        for (List<Point> lp : l) {
            boolean ok = false;
            for (Point p : lp) {
                int xp = (int) p.x + x;
                int yp = (int) p.y + y;
                if (xp >= 0 && xp < 37 && yp >= 0 && yp < 37 && idtc[xp][yp] == i) {
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

    private void extractColors() {
        Scalar a = new Scalar(0, 0, 255);
        Scalar b = new Scalar(0, 255, 0);
        List<Scalar> colors = m.getColors();
        //pega a as cores
        for (int x = 0; x < 37; x++) {
            for (int y = 0; y < 37; y++) {
                Point p = centers[x][y];
                Rect rect = new Rect((int) p.x - 3, (int) p.y + 1, 9, 9);/*verdepois*/
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
                Imgproc.rectangle(src, rect.br(), rect.tl(), idtc[x][y] < 11 ? b : a, 2);
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
            for (int r = 0; r < 4; r++) {
                List<List<Point>> l = regs.get(i).getReg(r);
                for (int x = 0; x < 37; x++) {
                    for (int y = 0; y < 37; y++) {
                        if (match(i, x, y, l)) {
                            idtc[x][y] = 12;
                            resul.add(new Result(i, r, x, y));
                            Point p = centers[x][y];
                            Rect rect = new Rect((int) p.x - 3, (int) p.y + 1, 9, 9);/*verdepois*/
                            Imgproc.rectangle(src, rect.br(), rect.tl(), b, 2);
                        }
                    }
                }
            }
        }
    }

    public BufferedImage getIm() {
        src = m.getMainFrame();
        if (src.empty()) {
            return ferr;
        }
        //Imgproc.drawContours(src, contours, -1, new Scalar(255, 255, 0));
        extractColors();
        extractObjs();
        return mat2BufferedImg(src);
    }

    public BufferedImage getIm2() {
        gp.setBackground(Color.WHITE);
        gp.clearRect(0, 0, im2.getWidth(), im2.getHeight());
        gp.setColor(Color.BLACK);
        for (int i = 0; i < 37; i++) {
            for (int j = 0; j < 37; j++) {
                gp.setFont(new Font("TimesRoman", Font.PLAIN, 20));
                gp.drawString(String.valueOf(idtc[i][j]), (i * 30 + 10), (j * 30 + 20));
            }
        }
        return im2;
    }

    public static BufferedImage mat2BufferedImg(Mat in) {
        int w = in.cols();
        int h = in.rows();
        byte[] data = new byte[w * h * (int) in.elemSize()];
        in.get(0, 0, data);
        BufferedImage out;
        if (in.elemSize() == 3) {
            for (int i = 0; i < data.length; i += 3) {
                byte redChannel = data[i];
                byte blueChannel = data[i + 2];
                data[i] = blueChannel;
                data[i + 2] = redChannel;
            }
            out = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        }
        out.getRaster().setDataElements(0, 0, w, h, data);
        return out;
    }

    public static void main(String args[]) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainW.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        new MainC().init();
    }

    @Override
    public void update(Observable ob, Object o) {
        if (ob == m && o.getClass() == Integer.class) {
            switch ((int) o) {
                case 0:
                    List<MatOfPoint> contours = new ArrayList<>();
                    Imgproc.findContours(m.getMask(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                    mapPoints(contours);
                    v.getCapBtn().setEnabled(m.getColors().size() == m.objsSize());
                    break;
                case 1:
                    v.getCapBtn().setEnabled(m.getColors().size() == m.objsSize());
                    break;
                case 2:
                    v.getCapBtn().setEnabled(m.getColors().size() == m.objsSize());
                    break;
            }
        }
    }
}
