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

import com.hazelcast.core.HazelcastInstance;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.karaf.cellar.core.ClusterManager;
import org.apache.karaf.cellar.core.GroupManager;
import org.apache.karaf.cellar.core.Node;
import org.apache.karaf.cellar.core.command.ExecutionContext;
import org.apache.karaf.cellar.core.control.ManageGroupAction;
import org.apache.karaf.cellar.core.control.ManageGroupCommand;
import org.apache.karaf.cellar.core.control.ManageGroupResult;
import org.apache.karaf.cellar.core.event.EventProducer;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 *
 * Balancer controller.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class Controller {
    
    /* Controller Instance */
    private static Controller instance = null;
    /* Hazelcast Instance */
    private HazelcastInstance hazelcast_instance;
    /* Execution Context Instance */
    private ExecutionContext execution_context;
    /* EventProducer Instance */
    private EventProducer event_producer;
    /* Cluster Manager Instance */
    protected ClusterManager cluster_manager;
    /* Group Manager Instance */
    protected GroupManager group_manager;
    /* Configuration Admin Instance */
    protected ConfigurationAdmin configuration_admin;
    /* List of Host */
    private final Set<Host> host_list;
    /* Array of Groups */
    private final Map<String, Group> group_list;
    
    /**
     * 
     * Create a new Controller instance.
     * 
     */
    private Controller(){
        /* Those settings will be setted by blueprint */
        hazelcast_instance = null;
        execution_context = null;
        event_producer = null;
        cluster_manager = null;
        group_manager = null;
        configuration_admin = null;
        
        /* Create the list of hosts */
        host_list = new HashSet<Host>();
        
        /* Create the list of groups */
        group_list = new HashMap<String, Group>();
    }
    
    /**
     * Return the instance of FoT Balance Controller.
     * 
     * @return A FoT balance controller.
     */
    public static Controller getInstance(){
        /* If the controller is not initialized yet, create a new instance of controller */
        if(instance == null){
            instance = new Controller();
        }
        
        /* Return the instance */
        return instance;
    }
    
    /**
     * 
     * Register a new group.
     * 
     * @param group_name The name of the group to be registered.
     */
    public void addGroup(String group_name){
        /* Create a new group with the given group name */
        Group group = new Group(group_name);
        
        /* Add the created group to the map */
        group_list.put(group_name, group);
        
        /* Register the group on cellar */
        createCellarGroup(group_name);
    }
    
    /**
     * 
     * Remove a group from cellar.
     * 
     * @param group_name Name of the group to be removed.
     */
    public void removeGroup(String group_name){
        /* Get the group with the given group name */
        Group group = getGroup(group_name);
        
        /* Remove all hosts from group */
        group.removeAllHosts();
        
        /* Unregister this group from cellar */
        removeCellarGroup(group_name);
    }

    /**
     * 
     * Add a host to a given group.
     * 
     * @param host
     * @param group 
     */
    public void addHostToGroup(Host host, Group group){
        
    }
    
    /**
     * 
     * Remove a host from a given group.
     * 
     * @param host
     * @param group 
     */
    public void removeHostFromGroup(Host host, Group group){
        
    }
    
    /**
     * 
     * Return a group, based on given name.
     * 
     * @param group_name The name of the desired group.
     * @return The group instance.
     */
    public Group getGroup(String group_name){
        return group_list.get(group_name);
    }

    /**
     * 
     * Create a cluster group on Cellar.
     * 
     * @param group_name The name of the group desired.
     */
    public void createCellarGroup(String group_name){
        /* Check if the group isn't present on the local host and then create it */
        if(!group_manager.isLocalGroup(group_name)){
            group_manager.createGroup(group_name);
        }
    }
    
    /**
     * 
     * Remove a given cluster group.
     * 
     * @param group_name The name of the group desired.
     */
    public void removeCellarGroup(String group_name){
        group_manager.deleteGroup(group_name);
    }
    
    /* Execute a given manage group command */
    private void executeManageGroupCommand(Node node, String group_name, ManageGroupAction manage_action){
        /* Create the destination set */
        Set<Node> destination = new HashSet<Node>();
        destination.add(node);
        
        /* Create and adjust the command to add the host to the given group */
        ManageGroupCommand command = new ManageGroupCommand(cluster_manager.generateId());
        command.setDestination(destination);
        command.setAction(manage_action);
        command.setGroupName(group_name);
        command.setSourceGroup(null);

        /* Result of the command execution */
        Map<Node, ManageGroupResult> results = null;
        
        try{
            /* Try to execute the command */
            results = execution_context.execute(command);
        }
        catch(Exception e){
            System.err.println("Something went wrong...");
            e.printStackTrace(new PrintStream(System.err));
        }
        
        /* Check result and print table */
        if (results == null || results.isEmpty()) {
            System.out.println("No result received within given timeout");
        }
        else {
            // <editor-fold defaultstate="collapsed" desc="Print table of Groups and Members">
            /* BEGIN OF PRINT FUNCTION */
            /* ----------------------------- */
            ShellTable table = new ShellTable();
            table.column(" ");
            table.column("Group");
            table.column("Members");

            for (Node n : results.keySet()) {
                ManageGroupResult result = results.get(node);
                if (result != null && result.getGroups() != null) {
                    for (org.apache.karaf.cellar.core.Group g : result.getGroups()) {
                        StringBuilder buffer = new StringBuilder();
                        if (g.getNodes() != null && !g.getNodes().isEmpty()) {
                            String local = "";
                            for (Node member : g.getNodes()) {
                                // display only up and running nodes in the cluster
                                if (cluster_manager.findNodeById(member.getId()) != null) {
                                    buffer.append(member.getId());                                    
                                    if (member.equals(cluster_manager.getNode())) {
                                        local = "x";
                                        buffer.append("(x)");
                                    }
                                    buffer.append(" ");
                                }
                            }
                            table.addRow().addContent(local, g.getName(), buffer.toString());
                        } else {
                            table.addRow().addContent("", g.getName(), "");
                        }
                    }
                }
            }
            table.print(System.out);
            /* ----------------------------- */
            /* END OF PRINT FUNCTION */
            // </editor-fold>
        }
    }
    
    /**
     * 
     * Add a given host to a cellar group.
     * 
     * @param node Node to be added.
     * @param group_name Group that will store the node.
     */
    public void addHostsCellarGroup(Node node, String group_name){        
        executeManageGroupCommand(node, group_name, ManageGroupAction.JOIN);
    }

    /**
     * 
     * Remove a host from a given cellar group.
     * 
     * @param node Host that will be removed.
     * @param group_name Group name from which node will be removed.
     */
    public void removeHostCellarGroup(Node node, String group_name){
        executeManageGroupCommand(node, group_name, ManageGroupAction.QUIT);
    } 
    
    // <editor-fold defaultstate="collapsed" desc="Basic Getter and Setter Functions">

    /**
     * 
     * Get Hazelcast Instance.
     * 
     * @return Return the hazelcast instance.
     */
    public HazelcastInstance getHazelcastInstance() {
        return hazelcast_instance;
    }

    /**
     * 
     * Set a new Hazelcast Instance.
     * 
     * @param hazelcast_instance The hazelcast instance that will be added.
     */
    public void setHazelcastInstance(HazelcastInstance hazelcast_instance) {
        this.hazelcast_instance = hazelcast_instance;
    }

    /**
     * 
     * Return a execution context.
     * 
     * @return Execution context.
     */
    public ExecutionContext getExecutionContext() {
        return execution_context;
    }

    /**
     * 
     * Set the execution context.
     * 
     * @param execution_context The execution context that will be added.
     */
    public void setExecutionContext(ExecutionContext execution_context) {
        this.execution_context = execution_context;
    }

    /**
     * 
     * Get event producer.
     * 
     * @return Event producer.
     */
    public EventProducer getEventProducer() {
        return event_producer;
    }

    /**
     * 
     * Set event producer.
     * 
     * @param event_producer The event producer that will be added.
     */
    public void setEventProducer(EventProducer event_producer) {
        this.event_producer = event_producer;
    }

    /**
     * 
     * Get cluster manager.
     * 
     * @return Cluster manager.
     */
    public ClusterManager getClusterManager() {
        return cluster_manager;
    }

    /**
     * 
     * Set cluster manager.
     * 
     * @param cluster_manager The cluster manager that will be added.
     */
    public void setClusterManager(ClusterManager cluster_manager) {
        this.cluster_manager = cluster_manager;
    }

    /**
     * 
     * Get group manager.
     * 
     * @return Group Manager.
     */
    public GroupManager getGroupManager() {
        return group_manager;
    }

     /**
     * 
     * Set group manager.
     * 
     * @param group_manager The Group Manager that will be added.
     */
    public void setGroupManager(GroupManager group_manager) {
        this.group_manager = group_manager;
    }

     /**
     * 
     * Get configuration admin.
     * 
     * @return Configuration Admin.
     */
    public ConfigurationAdmin getConfigurationAdmin() {
        return configuration_admin;
    }

    /**
     * 
     * Set configuration admin.
     * 
     * @param configuration_admin 
     */
    public void setConfigurationAdmin(ConfigurationAdmin configuration_admin) {
        this.configuration_admin = configuration_admin;
    }
    
    // </editor-fold>

}
