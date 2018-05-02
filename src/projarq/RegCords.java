/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilherme Gama
 */
public class RegCords {

    public List<RegCord> l = new ArrayList<>();

    public List<List<Ponto>> get(int i, int r, int x, int y) {
        return l.get(i).getReg(r, x, y);
    }

    public String getName(int i) {
        return l.get(i).n;
    }

    public int getClas(int i) {
        return l.get(i).clas;
    }

    public int size() {
        return l.size();
    }

    public boolean add(RegCord r) {
        return l.add(r);
    }

    void remove(int i) {
        l.remove(i);
    }

    void set(int i, RegCord r) {
        l.set(i, r);
    }
}
