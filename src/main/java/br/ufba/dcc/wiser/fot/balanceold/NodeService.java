/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balanceold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jurandir
 */
public class NodeService {

    Map<String, List<String>> serviceNode;

    public NodeService() {
        List<String> services;
        serviceNode = new HashMap<String, List<String>>();
        //node 1
        services = new ArrayList<String>();
        services.add("app1_sec-fatorial-1.0.jar");
        services.add("app2_sec-fatorial-1.0.jar");
        services.add("app1_dis-fatorial-1.0.jar");
        services.add("app2_dis-fatorial-1.0.jar");
        serviceNode.put("192.18.0.58", services);

        //node 2
        services = new ArrayList<String>();
        services.add("app3_sec-fatorial-1.0.jar");
        services.add("app1_loc-fatorial-1.0.jar");
        services.add("app2_loc-fatorial-1.0.jar");
        services.add("app3_loc-fatorial-1.0.jar");
        serviceNode.put("192.18.0.68", services);

        //node 3
        services = new ArrayList<String>();
        services.add("app1_com-fatorial-1.0.jar");
        services.add("app2_com-fatorial-1.0.jar");
        services.add("app4_loc-fatorial-1.0.jar");
        serviceNode.put("192.18.0.78", services);

        //node 4
        services = new ArrayList<String>();
        services.add("app3_com-fatorial-1.0.jar");
        services.add("app4_com-fatorial-1.0.jar");
        services.add("app3_dis-fatorial-1.0.jar");
        services.add("app4_dis-fatorial-1.0.jar");
        serviceNode.put("192.18.0.88", services);

        //node 5
        services = new ArrayList<String>();
        services.add("app1_man-fatorial-1.0.jar");
        services.add("app1_sto-fatorial-1.0.jar");
        serviceNode.put("192.18.0.98", services);

        for (String key : serviceNode.keySet()) {
            List<String> value = serviceNode.get(key);
            System.out.println(key + " = " + value);
        }

    }

    public void addNode(String ip, List service) {
        serviceNode.put(ip, service);
    }

    public void removeNode(String ip) {
        serviceNode.remove(ip);
    }
}
