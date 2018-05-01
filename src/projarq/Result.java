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
public class Result {

    String n;
    public int t;
    public int r;
    List<Ponto> l = new ArrayList<>();

    Result(String name, int type, int rot, List<Ponto> list) {
        n = name;
        t = type;
        r = rot;
        l.addAll(list);
    }
}
