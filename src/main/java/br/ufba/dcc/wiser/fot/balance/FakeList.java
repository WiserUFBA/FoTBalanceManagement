/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balance;

import java.util.ArrayList;
/**
 *
 * @author juran
 */
public class FakeList {

    public static void main(String[] args) {
    
    // [ A ] declarando e instanciando uma lista de IP's
    ArrayList<String> ip = new ArrayList();
    
    // [ B ] usando o método add() para gravar 5 números de IP
    ip.add("192.168.0.1");
    ip.add("192.168.0.2");
    ip.add("192.168.0.3");
    ip.add("192.168.0.4");
    ip.add("192.168.0.5");
    
    

    
    //System.out.println( Arrays.toString(ip.toArray()));
  }

}
