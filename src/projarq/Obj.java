/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.io.Serializable;

/**
 *
 * @author Guilherme Gama
 */
public class Obj implements Serializable {

    public Info[][] m;
    public String n;
    public int clas;

    Obj(String name, int c, Info[][] mat) {
        clas = c;
        m = new Info[mat.length][mat.length];
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat.length; j++) {
                m[i][j] = new Info(mat[i][j].t, mat[i][j].x, mat[i][j].y);
            }
        }
        n = name;
    }
}
