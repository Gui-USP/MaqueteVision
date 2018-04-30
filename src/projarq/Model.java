/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import javax.swing.DefaultListModel;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Guilherme Gama
 */
public class Model extends Observable {

    private static final String OBJPATH = "src/projarq/objs";
    private static final String CALIB1PATH = "src/projarq/calib1.bmp";
    private static final String CALIB2PATH = "src/projarq/calib2";
    private VideoCapture webSource = new VideoCapture(0);
    private int camInd = 0;
    private Mat frame = new Mat();
    private Mat framep = new Mat();
    Mat mask;
    List<Scalar> colors = new ArrayList<>();
    List<Obj> objs = new ArrayList<>();
    RegCords regcords = new RegCords();
    DefaultListModel<String> objModel = new DefaultListModel<>();
    boolean fromWeb = false;
    private Mat calib1;
    private Mat calib2;
    private Mat main;

    public void startThread() {
        Thread t = new Thread() {
            long time = 0, fps = 0, start;

            @Override
            public void run() {
                while (true) {
                    start = System.nanoTime();
                    if (webSource.isOpened()) {
                        webSource.read(framep);
                        frame = framep.submat(0, 1080, 420, 1500);
                    }
                    try {
                        long s = 33 - (System.nanoTime() - start) / 1000000;
                        if (s > 0) {
                            sleep(s);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("sleep model error");
                    }
                    time += System.nanoTime() - start;
                    fps++;
                    if (time > 1000000000) {
                        //System.out.println("FPS model " + fps);
                        time = fps = 0;
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    Model() {
        Mat temp = imread("src/projarq/calib1.jpg", Imgcodecs.IMREAD_COLOR);
        calib1 = temp.empty() ? temp : temp.submat(0, 1080, 420, 1500);
        temp = imread("src/projarq/calib2.jpg", Imgcodecs.IMREAD_COLOR);
        calib2 = temp.empty() ? temp : temp.submat(0, 1080, 420, 1500);
        temp = imread("src/projarq/main.jpg", Imgcodecs.IMREAD_COLOR);
        main = temp.empty() ? temp : temp.submat(0, 1080, 420, 1500);
        webSource.set(3, 1920);
        webSource.set(4, 1080);
    }

    public void load() {
        loadObjs();
        loadMask();
        loadColors();
    }

    public Mat getMask() {
        return mask;
    }

    public void setMask(Mat m) {
        mask = m;
        imwrite(CALIB1PATH, mask);
        setChanged();
        notifyObservers(0);
    }

    private void loadMask() {
        mask = imread(CALIB1PATH, Imgcodecs.IMREAD_GRAYSCALE);
        setChanged();
        notifyObservers(0);
    }

    public List<Scalar> getColors() {
        return colors;
    }

    public void setColors(List<Scalar> c) {
        colors = c;
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CALIB2PATH))) {
            for (Scalar s : colors) {
                out.writeObject(s.val);
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Salvando "+ colors.size()+" cores");
        setChanged();
        notifyObservers(1);
    }

    @SuppressWarnings("unchecked")
    private void loadColors() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(CALIB2PATH))) {
            while (true) {
                Scalar s = new Scalar((double[]) in.readObject());
                colors.add(s);
            }
        } catch (FileNotFoundException ex) {
            System.out.println("não existe o arquivo de cores!");
        } catch (IOException ex) {
            System.out.println("Cabou de ler, pegou " + colors.size() + " cores");
        } catch (ClassNotFoundException ex) {
            System.out.println("Nenhuma cor salva!");
        }
        setChanged();
        notifyObservers(1);
    }

    public Obj getObj(int i) {
        return objs.get(i);
    }

    public void addObj(Obj o) {
        objs.add(o);
        regcords.add(new RegCord(o));
        objModel.addElement(o.n);
        writeObjs();

        setChanged();
        notifyObservers(2);
    }

    public void remObj(int i) {
        objs.remove(i);
        regcords.remove(i);
        objModel.remove(i);
        writeObjs();

        setChanged();
        notifyObservers(2);
    }

    public void editObj(int ind, String name, Info[][] m) {
        Obj o = objs.get(ind);
        o.n = name;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                o.m[i][j].t = m[i][j].t;
                o.m[i][j].x = m[i][j].x;
                o.m[i][j].y = m[i][j].y;
            }
        }

        regcords.set(ind, new RegCord(o));
        objModel.setElementAt(name, ind);
        writeObjs();

        setChanged();
        notifyObservers(2);
    }

    @SuppressWarnings("unchecked")
    private void loadObjs() {
        try {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(OBJPATH))) {
                while (true) {
                    Obj o = (Obj) in.readObject();
                    objs.add(o);
                    regcords.add(new RegCord(o));
                    System.out.println(o.n);
                    objModel.addElement(o.n);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Não existe o arquivo de objs!");
        } catch (IOException ex) {
            System.out.println("Cabou de ler, pegou " + objs.size() + " objs");
        } catch (ClassNotFoundException ex) {
            System.out.println("Nenhum objeto salvo!");
        }

        setChanged();
        notifyObservers(2);
    }

    private void writeObjs() {
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(OBJPATH))) {
                for (Obj o : objs) {
                    out.writeObject(o);
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    public RegCords getRegs() {
        return regcords;
    }

    public DefaultListModel<String> getObjModel() {
        return objModel;
    }

    void release() {
        webSource.release();
    }

    Mat getCalib1Frame() {
        return fromWeb? frame.clone() : calib1.clone();
    }

    Mat getCalib2Frame() {
        return fromWeb? frame.clone() : calib2.clone();
    }

    Mat getMainFrame() {
        return fromWeb? frame.clone() : main.clone();
    }

    public int nextCam() {
        camInd = (camInd + 1) % 5;
        webSource.open(camInd);
        webSource.set(3, 1920);
        webSource.set(4, 1080);
        return camInd;
    }

    boolean repObj(String s) {
        return !objs.stream().noneMatch((o) -> (o.n.equals(s)));
    }

    boolean rerepObj(String s, int i) {
        return i != -1 && !objs.stream().noneMatch((Obj o) -> o.n.equals(s) && !s.equals(objModel.get(i)));
    }

    int objsSize() {
        return objs.size();
    }
}
