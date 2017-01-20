/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balanceold;

import java.util.ArrayList;

/**
 *
 * @author juran
 */

public class NodeIP {
    private String ip;
    private ArrayList<String> bundle = new ArrayList<String>();

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public ArrayList<String> getBundle() {
        return bundle;
    }

    public void setBundle(ArrayList<String> bundle) {
        this.bundle = bundle;
    }
    
}
