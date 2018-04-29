/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projarq;

/**
 *
 * @author Guilherme Gama
 */
public class Result {
        public int t;
        public int r;
        public int x;
        public int y;
        
        public Result(int type, int rot, int xpos, int ypos){
            t = type;
            r = rot;
            x = xpos;
            y = ypos;
        }
}
