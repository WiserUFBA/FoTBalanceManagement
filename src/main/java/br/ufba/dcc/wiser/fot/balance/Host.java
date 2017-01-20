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

import org.apache.karaf.cellar.core.Node;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Host of FoT Balance Management.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class Host {
    /* Hazelcast host instance */
    private final Node host_hazelcast_instance;
    /* List of subscription groups */
    private final Set<Group> group_list;
    /* Capacity of this host */
    private int host_capacity;
    
    /**
     * 
     * Instantiate a FoT Balance Host.
     * 
     * @param host_hazelcast_instance Reference of hazelcast host.
     * @param host_capacity Capacity of this node.
     */
    public Host(Node host_hazelcast_instance, int host_capacity){
        /* Store hazelcast host instance */
        this.host_hazelcast_instance = host_hazelcast_instance;
        
        /* Set the host capacity */
        this.host_capacity = host_capacity;
        
        /* Create a list of group */
        group_list = new HashSet<>();
    }
    
    /**
     * 
     * Get the host instance.
     * 
     * @return Hazelcast Host instance.
     */
    public Node getHostInstance(){
        return host_hazelcast_instance;
    }
    
    /**
     * 
     * Add a group to the given host.
     * 
     * @param group_name Name of the group that will be added.
     */
    public void addGroup(String group_name){
        /* Retrieve the controller instance */
        Controller controller_instance = Controller.getInstance();
        
        /* Get the Group instance */
        Group group;
        try{
            group = controller_instance.getGroup(group_name);
        }
        catch(NullPointerException e){
            /* Stop Application if the group not exist */
            System.err.println("Invalid group or group not exist.");
            return;
        }
        
        /* Add the group retrieved */
        group_list.add(group);
    }
    
    /**
     * 
     * Remove a group from a given host.
     * 
     * @param group_name Name of the group that will be added.
     */
    public void removeGroup(String group_name){
        /* Retrieve the controller instance */
        Controller controller_instance = Controller.getInstance();
        
        /* Get the Group instance */
        Group group;
        try{
            group = controller_instance.getGroup(group_name);
        }
        catch(NullPointerException e){
            /* Stop Application if the group not exist */
            System.err.println("Invalid group or group not exist.");
            return;
        }
        
        /* Remove the group retrieved */
        group_list.remove(group);
    }
    
    /**
     * 
     * Remove All groups from the given host.
     * 
     */
    public void removeAllGroups(){
        /* Remove this host from all groups subscribed */
        for(Group group : group_list){
            group.removeHost(this);
        }
    }
    
    /**
     * 
     * Get the list of groups subscribed.
     * 
     * @return A list of groups subscribed.
     */
    public Set<Group> getGroupList(){
        return group_list;
    }
    
    /**
     * 
     * Get the host capacity.
     * 
     * @return The host capacity
     */
    public int getHostCapacity(){
        return host_capacity;
    }
    
    /**
     * 
     * Set the capacity of the new host.
     * 
     * @param new_capacity New capacity.
     */
    public void setHostCapacity(int new_capacity){
        this.host_capacity = new_capacity;
    }
    
}
