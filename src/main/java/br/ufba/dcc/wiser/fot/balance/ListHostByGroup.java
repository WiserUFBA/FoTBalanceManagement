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
public class ListHostByGroup {
    
    String groupName;
    ArrayList<String> hostName = new ArrayList<String>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getHostName() {
        return hostName;
    }
    
}
