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
        host_list = new HashSet<Host>();
        
        /* Create the bundle list */
        bundles_list = new HashSet<Bundles>();
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
        /* Set the new group name */
        this.group_name = group_name;
        
        /*  */
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
        
    }
    
    public void removeAllHosts(){
    }
    
}
