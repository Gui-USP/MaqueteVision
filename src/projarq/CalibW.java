/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;

/**
 *
 * @author Guilherme Gama
 */
public class CalibW extends javax.swing.JFrame {
    
    CalibC c;
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
                    camPan2.repaint();
                    try {
                        long s = 33 - (System.nanoTime()- start)/1000000;
                        if(s>0)
                            sleep(s);
                    } catch (InterruptedException ex) {
                        System.out.println("sleep calib error");
                    }
                    time += System.nanoTime() - start;
                    fps++;
                    if (time > 1000000000) {
                        //System.out.println("FPS calib " + fps);
                        time = fps = 0;
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
    
    public CalibW(CalibC cc, BufferedImage ini) {
        c = cc;
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
        calibBtn = new javax.swing.JButton();
        camPan2 = new javax.swing.JPanel(){
            public void paintComponent(Graphics g) {
                g.drawImage(im2, 0, 0, getWidth(), getHeight(), 0, 0, im2.getWidth(), im2.getHeight(), null);
            }
        };
        calibBtn2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout camPanLayout = new javax.swing.GroupLayout(camPan);
        camPan.setLayout(camPanLayout);
        camPanLayout.setHorizontalGroup(
            camPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 516, Short.MAX_VALUE)
        );
        camPanLayout.setVerticalGroup(
            camPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 482, Short.MAX_VALUE)
        );

        calibBtn.setText("Calibrar1");

        javax.swing.GroupLayout camPan2Layout = new javax.swing.GroupLayout(camPan2);
        camPan2.setLayout(camPan2Layout);
        camPan2Layout.setHorizontalGroup(
            camPan2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 698, Short.MAX_VALUE)
        );
        camPan2Layout.setVerticalGroup(
            camPan2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        calibBtn2.setText("Calibrar2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(calibBtn)
                        .addGap(54, 54, 54)
                        .addComponent(calibBtn2))
                    .addComponent(camPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(camPan2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(camPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(calibBtn2)
                            .addComponent(calibBtn))
                        .addGap(0, 152, Short.MAX_VALUE))
                    .addComponent(camPan2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public JButton getCalibBtn() {
        return calibBtn;
    }
    
    public JButton getCalib2Btn() {
        return calibBtn2;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calibBtn;
    private javax.swing.JButton calibBtn2;
    private javax.swing.JPanel camPan;
    private javax.swing.JPanel camPan2;
    // End of variables declaration//GEN-END:variables
}
