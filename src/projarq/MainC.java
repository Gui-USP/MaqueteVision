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
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.opencv.core.Core;
import static org.opencv.core.CvType.CV_8UC3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
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
    Ponto[][] centers = new Ponto[37][37];
    int[][] idtc = new int[37][37];

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

    public List<Ponto> getCalibPoints(int n) {
        List<Ponto> p = new ArrayList<>();
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

    public BufferedImage getIm() {
        src = m.getMainFrame();
        if (src.empty()) {
            return ferr;
        }
        idtc = Recognizer.extractColors(m.getColors(), src, centers);
        List<Result> res = Recognizer.extractAll(idtc, m.getRegs());
        for (int x = 0; x < 37; x++) {
            for (int y = 0; y < 37; y++) {
                Ponto p = centers[x][y];
                Rect rect = new Rect(p.x - 3, p.y + 1, 9, 9);//verdepois
                Imgproc.rectangle(src, rect.br(), rect.tl(), idtc[x][y] < 11 ? new Scalar(0, 255, 0) : new Scalar(0, 0, 255), 2);
            }
        }
        
        for(Result r : res){
            for(Ponto p : r.l){
                Rect rect = new Rect(centers[p.x][p.y].x - 3, centers[p.x][p.y].y + 1, 9, 9);//verdepois
                Imgproc.rectangle(src, rect.br(), rect.tl(), new Scalar(255, 0, 0), 2);
            }
        }
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
                    centers = PointMap.mapPoints(contours);
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
