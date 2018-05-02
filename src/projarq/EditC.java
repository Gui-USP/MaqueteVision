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

    Color fixa;
    Color var;
    Color centro;

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

        fixa = v.getBlauFixa().getBackground();
        var = v.getBlauVar().getBackground();
        centro = v.getBlauCentro().getBackground();

        checkName();
        checkObj();
        errUpdate();
    }

    Color getColor(int i) {
        return i == 0 ? Color.WHITE : i == 1 ? fixa : i == 2 ? var : centro;
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
        String s;
        if (objnameemp) {
            v.getErrLab().setText("Nome vazio!");
        } else if (objnamebig) {
            v.getErrLab().setText("Nome muito grande!");
        } else if (objemp) {
            v.getErrLab().setText("Peça vazia!");
        } else if (objwrong) {
            v.getErrLab().setText("Peça sem centro ou com mais de 1!");
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
        Obj o = m.getObj(i);
        v.getNameText().setText(o.n);
        ep.load(o.clas, o.m);

        checkName();
        checkObj();
        errUpdate();
    }

    private void addObj() {
        m.addObj(new Obj(v.getNameText().getText(), ep.getClas(), ep.mat));

        checkName();
        errUpdate();
    }

    private void remObj() {
        int ind = v.getList().getSelectedIndex();
        m.remObj(ind);

        checkName();
        errUpdate();
    }

    private void upObj() {
        int ind = v.getList().getSelectedIndex();
        Obj o = m.upObj(ind);
        v.getNameText().setText(o.n);
        ep.load(o.clas, o.m);
        

        checkName();
        errUpdate();
    }

    private void downObj() {
        int ind = v.getList().getSelectedIndex();
        Obj o = m.downObj(ind);
        v.getNameText().setText(o.n);
        ep.load(o.clas, o.m);
        

        checkName();
        errUpdate();
    }

    private void saveObj() {
        m.editObj(v.getList().getSelectedIndex(), ep.getClas(), v.getNameText().getText(), ep.mat);

        checkName();
        errUpdate();
    }

    private void setConection() {
        v.getList().setModel(m.getObjModel());
        v.getList().addListSelectionListener((ListSelectionEvent lse) -> {
            int ind = v.getList().getSelectedIndex();
            if (ind >= 0) {
                listSel(ind);
                v.getRemBtn().setEnabled(true);
                v.getDownBtn().setEnabled(ind < m.objModel.getSize() - 1);
                v.getUpBtn().setEnabled(ind > 0);
            } else {
                v.getRemBtn().setEnabled(false);
                v.getSaveBtn().setEnabled(false);
                v.getDownBtn().setEnabled(false);
                v.getUpBtn().setEnabled(false);
            }
        });
        v.getRemBtn().setEnabled(false);
        v.getSaveBtn().setEnabled(false);
        v.getDownBtn().setEnabled(false);
        v.getUpBtn().setEnabled(false);
        v.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                c.closedEdit();
            }
        });
        v.getAddBtn().addActionListener(ae -> addObj());
        v.getRemBtn().addActionListener(ae -> remObj());
        v.getSaveBtn().addActionListener(ae -> saveObj());
        v.getUpBtn().addActionListener(ae -> upObj());
        v.getDownBtn().addActionListener(ae -> downObj());
        v.getNormBtn().addActionListener(ae -> ep.setClas(0));
        v.getTypeBtnG().setSelected(v.getNormBtn().getModel(), true);
        v.getWallBtn().addActionListener(ae -> ep.setClas(1));
        v.getInBtn().addActionListener(ae -> ep.setClas(2));
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
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        v.getBlauFixa().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 1;
                v.getBlauSel().setBackground(v.getBlauFixa().getBackground());
            }
        });
        v.getBlauVar().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 2;
                v.getBlauSel().setBackground(v.getBlauVar().getBackground());
            }
        });
        v.getBlauApaga().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 0;
                v.getBlauSel().setBackground(v.getBlauApaga().getBackground());
            }
        });
        v.getBlauCentro().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ep.type = 3;
                v.getBlauSel().setBackground(v.getBlauCentro().getBackground());
            }
        });
    }

    int[] getVarSize(int[] vt) {
        return new VarDialog(v, true, vt).showDialog();
    }

}
