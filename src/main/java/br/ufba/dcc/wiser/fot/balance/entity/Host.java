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
package br.ufba.dcc.wiser.fot.balance.entity;

import br.ufba.dcc.wiser.fot.balance.Controller;
import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import java.util.HashMap;
import org.apache.karaf.cellar.core.Node;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    /* ID of This host */
    private String host_id;
    
    /**
     * 
     * Instantiate a FoT Balance Host.
     * 
     * @param host_hazelcast_instance Reference of hazelcast host.
     * @param host_id FQDN of this host.
     * @param host_capacity Capacity of this node.
     */
    public Host(Node host_hazelcast_instance, String host_id, int host_capacity){
        /* Store id name of this host */
        this.host_id = host_id;
        
        /* Store hazelcast host instance */
        this.host_hazelcast_instance = host_hazelcast_instance;
        
        /* Set the host capacity */
        this.host_capacity = host_capacity;
        
        /* Create a list of group */
        group_list = new HashSet<>();
    }
    
    /**
     * 
     * Instantiate a host without a hazelcast_install reference.
     * 
     * @param host_id FQDN of this host.
     * @param host_capacity Capacity of this host.
     */
    public Host(String host_id, int host_capacity){
        this(null, host_id, host_capacity);
    }
    
    /**
     * 
     * No argument constructor for clone operations.
     * 
     */
    public Host(){
        this("", 0);
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
     * Get Host Address.
     * 
     * @return Get hostname or IP if host doesn't have a FQDN.
     */
    public String getHostAddress(){
        if(host_hazelcast_instance == null){
            /* If there are no hazelcast instance return localhost */
            return "localhost";
        }
        return host_hazelcast_instance.getHost();
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
            FoTBalanceUtils.error("Invalid group or group not exist.");
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
            FoTBalanceUtils.error("Invalid group or group not exist.");
            return;
        }
        
        /* Remove the group retrieved */
        group_list.remove(group);
    }
    
    /**
     * 
     * Get all bundles associated with this host.
     * 
     * @return A set of bundles associated with this host.
     */
    public Set<Bundles> getBundlesAssociated(){
        /* List of bundles associated with this host */
        Set<Bundles> bundles_associated = new HashSet<>();
        
        /* Get all bundles associated with this host */
        for(Group group : group_list){
            bundles_associated.addAll(group.getBundlesAssociated(this));
        }
        
        return bundles_associated;
    }
    
    /**
     * 
     * Get all install urls of the bundles associated with this host.
     * 
     * @return A map of install urls by groupss.
     */
    public Map<String, List<String>> getAllInstalUrls(){
        Map<String, List<String>> install_urls_groups = new HashMap<>();
        
        /* Get all install ulrs of the bundles associated with this host */
        for(Group group : group_list){
            install_urls_groups.put(group.getGroupName(), group.getInstallUrls(this));
        }
        
        return install_urls_groups;
    }
    
    /**
     * 
     * Get all uninstall urls of the bundles associated with this host.
     * 
     * @return A map of uninstall urls by groupss.
     */
    public Map<String, List<String>> getAllUninstalUrls(){
        Map<String, List<String>> uninstall_urls_groups = new HashMap<>();
        
        /* Get all install ulrs of the bundles associated with this host */
        for(Group group : group_list){
            uninstall_urls_groups.put(group.getGroupName(), group.getUninstallUrls(this));
        }
        
        return uninstall_urls_groups;
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
     * Get id or FQDN of the actual host.
     * 
     * @return Id of this host.
     */
    public String getHostID() {
        return host_id;
    }

    /**
     * 
     * Set new FQDN of this host
     * 
     * @param host_id New Id of this host.
     */
    public void setHostID(String host_id) {
        this.host_id = host_id;
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
