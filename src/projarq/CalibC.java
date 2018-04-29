/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import static org.opencv.core.Core.FONT_HERSHEY_DUPLEX;
import static org.opencv.core.CvType.CV_8UC3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Guilherme Gama
 */
public class CalibC {

    CalibW v;
    MainC c;
    Model m;
    BufferedImage ferr = MainC.mat2BufferedImg(new Mat(new Size(220, 220), CV_8UC3, new Scalar(0, 255, 0)));

    CalibC(MainC c, Model m) {
        this.c = c;
        this.m = m;
        v = new CalibW(this, ferr);
        v.setVisible(true);
        v.startThread();
        setListeners();
    }

    public BufferedImage getIm() {
        Mat src = m.getCalib1Frame();
        Mat mask = new Mat();
        if (src!= null && !src.empty()) {
            Imgproc.cvtColor(src, mask, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(mask, mask, 127, 255, Imgproc.THRESH_BINARY_INV);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            v.getCalibBtn().setEnabled(contours.size() == 37 * 37);
            return MainC.mat2BufferedImg(mask);
        }
        v.getCalibBtn().setEnabled(false);
        return ferr;
    }

    public BufferedImage getIm2() {
        Mat src = m.getCalib2Frame();
        List<Point> cps = c.getCalibPoints(m.objsSize());
        if (cps != null && !src.empty() && m.getMask() != null) {
            for (int i = 0; i < cps.size(); i++) {
                Point p = cps.get(i);
                Rect rect = new Rect((int) p.x-5, (int) p.y-5, 10, 10);
                Imgproc.rectangle(src, rect.br(), rect.tl(), new Scalar(255, 255, 0), 2);
                Imgproc.putText(src, m.objModel.get(i), new Point(p.x + 14, p.y + 8), FONT_HERSHEY_DUPLEX, 1, new Scalar(0, 0, 255), 2);
            }
            v.getCalib2Btn().setEnabled(true);
            return MainC.mat2BufferedImg(src);
        }
        v.getCalib2Btn().setEnabled(false);
        return ferr;
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

    private void calibrate() {
        Mat mask = new Mat();
        Imgproc.cvtColor(m.getCalib1Frame(), mask, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mask, mask, 127, 255, Imgproc.THRESH_BINARY_INV);
        m.setMask(mask);
        m.gg = true;
    }

    private void calibrate2() {
        Mat src = m.getCalib2Frame();
        List<Scalar> colors = new ArrayList<>();
        List<Point> cps = c.getCalibPoints(m.objsSize());
        for (int i = 0; i < cps.size(); i++) {
            Point p = cps.get(i);
            Rect rect = new Rect((int) p.x-5, (int) p.y-5, 10, 10);
            Mat roi = src.submat(rect);
            colors.add(centroid(roi));
        }
        m.setColors(colors);
    }

    private void setListeners() {
        v.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                c.closedCalib();
            }
        });
        v.getCalibBtn().addActionListener((ActionEvent ae) -> {
            calibrate();
        });
        v.getCalib2Btn().addActionListener((ActionEvent ae) -> {
            calibrate2();
            v.dispose();
            c.closedCalib();
        });
    }
}
