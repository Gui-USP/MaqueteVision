/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Guilherme Gama
 */
public class EditC {

    public static final int OBJNAMEMAX = 25;

    boolean objnamerep = false;
    boolean objrenamerep = false;
    boolean objnameemp = false;
    boolean objnamebig = false;
    boolean objemp = false;
    boolean objwrong = false;

    Model m;
    EditW v;
    EditPanel ep;
    MainC c;

    EditC(MainC c, Model m) {
        this.c = c;
        this.m = m;

        v = new EditW(this);
        v.setVisible(true);
        ep = v.getEditPan();
        ep.setEditC(this);
        setConection();

        checkName();
        checkObj();
        errUpdate();
    }

    private void checkName() {
        String s = v.getNameText().getText();
        objnameemp = s.isEmpty();
        objnamebig = s.length() > OBJNAMEMAX;
        objnamerep = m.repObj(s);
        objrenamerep = m.rerepObj(s, v.getList().getSelectedIndex());
    }

    public void checkObj() {
        objemp = ep.empty();
        objwrong = ep.wrong();
    }

    public void errUpdate() {
        v.getAddBtn().setEnabled(false);
        v.getSaveBtn().setEnabled(false);
        if (objnameemp) {
            v.getErrLab().setText("Nome vazio!");
        } else if (objnamebig) {
            v.getErrLab().setText("Nome muito grande!");
        } else if (objemp) {
            v.getErrLab().setText("Peça vazia!");
        } else if (objwrong) {
            v.getErrLab().setText("Peça sem centro!");
        } else if (objrenamerep) {
            v.getErrLab().setText("Nome repetido!");
        } else {
            v.getSaveBtn().setEnabled(v.getList().getSelectedIndex() != -1);
            if (objnamerep) {
                v.getErrLab().setText("Nome repetido!");
            } else {
                v.getErrLab().setText("");
                v.getAddBtn().setEnabled(true);
            }
        }
    }

    private void listSel(int i) {
        v.getRemBtn().setEnabled(true);
        Obj o = m.getObj(i);
        v.getNameText().setText(o.n);
        ep.load(o.m);

        checkName();
        checkObj();
        errUpdate();
    }

    private void addObj() {
        m.addObj(new Obj(v.getNameText().getText(), ep.mat));

        checkName();
        errUpdate();
    }

    private void remObj() {
        int ind = v.getList().getSelectedIndex();
        m.remObj(ind);

        checkName();
        errUpdate();
    }

    private void saveObj() {
        m.editObj(v.getList().getSelectedIndex(), v.getNameText().getText(), ep.mat);
        
        checkName();
        errUpdate();
    }

    private void setConection() {
        v.getList().setModel(m.getObjModel());
        v.getList().addListSelectionListener((ListSelectionEvent lse) -> {
            int ind = v.getList().getSelectedIndex();
            if (ind >= 0) {
                listSel(ind);
            } else {
                v.getRemBtn().setEnabled(false);
                v.getSaveBtn().setEnabled(false);
            }
        });
        v.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                c.closedEdit();
            }
        });
        v.getAddBtn().addActionListener((ActionEvent ae) -> {
            addObj();
        });
        v.getRemBtn().addActionListener((ActionEvent ae) -> {
            remObj();
        });
        v.getSaveBtn().addActionListener((ActionEvent ae) -> {
            saveObj();
        });
        v.getNameText().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                checkName();
                errUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                checkName();
                errUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        v.getBlauFixa().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 1;
                v.getBlauSel().setBackground(Color.DARK_GRAY);
            }
        });
        v.getBlauVar().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 2;
                v.getBlauSel().setBackground(Color.GRAY);
            }
        });
        v.getBlauApaga().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 0;
                v.getBlauSel().setBackground(Color.WHITE);
            }
        });
        v.getBlauCentro().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 3;
                v.getBlauSel().setBackground(Color.BLACK);
            }
        });
    }

    int[] getVarSize(int[] vt) {
        return new VarDialog(v, true, vt).showDialog();
    }

}
