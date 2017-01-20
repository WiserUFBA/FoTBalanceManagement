/*
 * The MIT License
 *
 * Copyright 2017 Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.ufba.dcc.wiser.fot.balance;

import java.util.HashSet;
import java.util.Set;
import org.apache.karaf.cellar.core.Node;

/**
 *
 * Group of hosts and bundles.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class Group {
    /* Name of the group */
    private String group_name;
    /* List of hosts */
    private final Set<Host> host_list;
    /* List of Bundles */
    private final Set<Bundles> bundles_list;

    /**
     * 
     * Create a FoT Balance Group.
     * 
     * @param group_name Name of the given group.
     */
    public Group(String group_name) {
        /* Set group name */
        this.group_name = group_name;
        
        /* Create the host list */
        host_list = new HashSet<>();
        
        /* Create the bundle list */
        bundles_list = new HashSet<>();
    }
    
    /**
     * 
     * Return the name of the group.
     * 
     * @return Group name.
     */
    public String getGroupName(){
        return group_name;
    }
    
    /**
     * 
     * Set the group name.
     * 
     * @param group_name The new group name.
     */
    public void setGroupName(String group_name){
        /* If the groupName is equal actual name, nothing change */
        if(this.group_name.equals(group_name)){
            System.err.println("Group already exists!");
            return;
        }
        
        /* Store the old group name */
        String old_group_name = this.group_name;
        
        /* Set the new group name */
        this.group_name = group_name;
        
        /* Get Controller Instance */
        Controller controller = Controller.getInstance();
        
        /* Register the new group */
        controller.addGroup(group_name);
        
        /* Remove from old group and put it on the newer */
        for(Host host : host_list){
            /* Get node from host */
            Node node = host.getHostInstance();

            /* Remove from old group name */
            controller.removeHostCellarGroup(node, old_group_name);
            
            /* Add to the newer group */
            controller.addHostCellarGroup(node, group_name);
        }
        
        /* Remove group */
        controller.removeCellarGroup(old_group_name);
    }
    
    /**
     * 
     * Add a host to this group.
     * 
     * @param host Host to add.
     */
    public void addHost(Host host){
        /* Add the host to the host list */
        host_list.add(host);
    }
    
    /**
     * 
     * Remove a given host from the group.
     * 
     * @param host Host that will be removed.
     */
    public void removeHost(Host host){
        /* Remove the host from the host list */
        host_list.remove(host);
    }
    
    /**
     * 
     * Remove and unregister all hosts from this group.
     * 
     */
    public void removeAllHosts(){
        /* Get the instance of the controller */
        Controller controller = Controller.getInstance();
        
        /* For each host on host list */
        for(Host host : host_list){
            /* Get the node object from host instance */
            Node node = host.getHostInstance();
            
            /* Remove the host */
            removeHost(host);
            
            /* Remove the node from the cellar group name */
            controller.removeHostCellarGroup(node, group_name);
        }
    }
    
    
    
}
