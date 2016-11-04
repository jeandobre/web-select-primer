package controllers;

/**
 * Created by jeandobre on 04/11/16.
 */
public class LCE {

    public Integer i;
    public Integer j;
    public Integer lce;

    static public Integer lce(char[] alfa, char[] beta, Integer i, Integer j){
        Integer o = 0;
        while(i + o < alfa.length && j + o < beta.length && alfa[o + i] == beta[o + j]){
            o++;
        }
        return o;
    }

    public LCE(Integer i, Integer j, Integer lce) {
        this.i = i;
        this.j = j;
        this.lce = lce;
    }
}
