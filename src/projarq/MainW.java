/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 *
 * @author Guilherme Gama
 */
public class MainW extends javax.swing.JFrame {

    MainC c;
    BufferedImage im;
    BufferedImage im2;

    public void startThread() {
        Thread t = new Thread(){
            long time = 0, fps = 0, start;

            @Override
            public void run() {
                while (true) {
                    start = System.nanoTime();
                    im = c.getIm();
                    im2 = c.getIm2();
                    camPan.repaint();
                    deb.repaint();
                    try {
                        long s = 33 - (System.nanoTime()- start)/1000000;
                        if(s>0)
                            sleep(s);
                    } catch (InterruptedException ex) {
                        System.out.println("sleep main error");
                    }
                    time += System.nanoTime() - start;
                    fps++;
                    if (time > 1000000000) {
                        //System.out.println("FPS main " + fps);
                        time = fps = 0;
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    public MainW(MainC mc, BufferedImage ini) {
        c = mc;
        im = ini;
        im2 = ini;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        camPan = new javax.swing.JPanel(){
            public void paintComponent(Graphics g) {
                g.drawImage(im, 0, 0, getWidth(), getHeight(), 0, 0, im.getWidth(), im.getHeight(), null);
            }
        };
        camNum = new javax.swing.JLabel();
        nextBtn = new javax.swing.JButton();
        capBtn = new javax.swing.JButton();
        deb = new javax.swing.JPanel(){
            public void paintComponent(Graphics g) {
                g.drawImage(im2, 0, 0, getWidth(), getHeight(), 0, 0, im2.getWidth(), im2.getHeight(), null);
            }
        };
        fromWebBtn = new javax.swing.JButton();
        fromArqBtn = new javax.swing.JButton();
        menBar = new javax.swing.JMenuBar();
        optMenu = new javax.swing.JMenu();
        calibBtn = new javax.swing.JMenuItem();
        editBtn = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout camPanLayout = new javax.swing.GroupLayout(camPan);
        camPan.setLayout(camPanLayout);
        camPanLayout.setHorizontalGroup(
            camPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 753, Short.MAX_VALUE)
        );
        camPanLayout.setVerticalGroup(
            camPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        camNum.setText("Webcam: 0");

        nextBtn.setText("Próxima");

        capBtn.setText("Capturar");

        javax.swing.GroupLayout debLayout = new javax.swing.GroupLayout(deb);
        deb.setLayout(debLayout);
        debLayout.setHorizontalGroup(
            debLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 887, Short.MAX_VALUE)
        );
        debLayout.setVerticalGroup(
            debLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        fromWebBtn.setText("da webcam");

        fromArqBtn.setText("dos arquivos");

        optMenu.setText("Opções");

        calibBtn.setText("Calibrar");
        optMenu.add(calibBtn);

        editBtn.setText("Editar Componentes");
        optMenu.add(editBtn);

        menBar.add(optMenu);

        setJMenuBar(menBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(camPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fromWebBtn)
                    .addComponent(nextBtn)
                    .addComponent(camNum)
                    .addComponent(fromArqBtn)
                    .addComponent(capBtn))
                .addGap(32, 32, 32)
                .addComponent(deb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(47, Short.MAX_VALUE)
                .addComponent(fromArqBtn)
                .addGap(11, 11, 11)
                .addComponent(fromWebBtn)
                .addGap(18, 18, 18)
                .addComponent(nextBtn)
                .addGap(18, 18, 18)
                .addComponent(camNum)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(capBtn)
                .addGap(570, 570, 570))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(camPan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public JButton getFromArqBtn() {
        return fromArqBtn;
    }

    public JButton getFromWebBtn() {
        return fromWebBtn;
    }

    public JMenuItem getEditBtn() {
        return editBtn;
    }

    public JMenuItem getCalibBtn() {
        return calibBtn;
    }

    public JButton getNextBtn() {
        return nextBtn;
    }

    public JLabel getCamNum() {
        return camNum;
    }

    public JButton getCapBtn() {
        return capBtn;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem calibBtn;
    private javax.swing.JLabel camNum;
    private javax.swing.JPanel camPan;
    private javax.swing.JButton capBtn;
    private javax.swing.JPanel deb;
    private javax.swing.JMenuItem editBtn;
    private javax.swing.JButton fromArqBtn;
    private javax.swing.JButton fromWebBtn;
    private javax.swing.JMenuBar menBar;
    private javax.swing.JButton nextBtn;
    private javax.swing.JMenu optMenu;
    // End of variables declaration//GEN-END:variables

}
