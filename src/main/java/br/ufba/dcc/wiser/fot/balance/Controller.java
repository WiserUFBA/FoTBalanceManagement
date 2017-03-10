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

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apache.karaf.cellar.bundle.BundleState;
import org.apache.karaf.cellar.bundle.ClusterBundleEvent;
import org.apache.karaf.cellar.bundle.Constants;
import org.apache.karaf.cellar.core.CellarSupport;
import org.apache.karaf.cellar.core.ClusterManager;
import org.apache.karaf.cellar.core.Configurations;
import org.apache.karaf.cellar.core.GroupManager;
import org.apache.karaf.cellar.core.Node;
import org.apache.karaf.cellar.core.command.ExecutionContext;
import org.apache.karaf.cellar.core.control.ManageGroupAction;
import org.apache.karaf.cellar.core.control.ManageGroupCommand;
import org.apache.karaf.cellar.core.control.ManageGroupResult;
import org.apache.karaf.cellar.core.control.SwitchStatus;
import org.apache.karaf.cellar.core.event.EventProducer;
import org.apache.karaf.cellar.core.event.EventType;
import org.apache.karaf.cellar.hazelcast.HazelcastNode;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.framework.BundleEvent;
import org.osgi.service.cm.ConfigurationAdmin;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

/**
 *
 * Balancer controller.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
@PlanningSolution
@XStreamAlias("Controller")
public class Controller implements Solution<HardSoftScore> {
    
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
    /* Set of Host */
    private final Set<Host> host_list;
        /* Set of bundles */
    private final Set<Bundles> bundle_list;
    /* Array of Groups */
    private final Map<String, Group> group_list;
    
    /* Default Node Capacity */
    public static int NODE_CAPACITY = 6;    
    
    /* Solver configuration file */
    public static final String SOLVER_CONFIGURATION = "br/ufba/dcc/wiser/fot/balance/solver/fotBalanceSolverConfig.xml";
    
    /* Config file */
    public static final String CONFIG_CLASSPATH_URL = "br/ufba/dcc/wiser/fot/balance/config/balance_config.json";
    
    /* Karaf Install Port, used by bundles */
    public static final int KARAF_INSTALL_PORT = 8181;
    
    /* Balance Score */
    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private HardSoftScore balance_score;
    
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
        host_list = new HashSet<>();
        
        /* Create the list of groups */
        group_list = new HashMap<>();
        
        /* Create a set of bundles */
        bundle_list = new HashSet<>();
        
        /* Instantiate a solver for balancing */
        SolverFactory<Controller> solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIGURATION);
        Solver<Controller> solver = solverFactory.buildSolver();        
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
     * Initialize application and execute some routines
     * 
     */
    public void init(){
        //JSON config_file = new JSON(CONFIG_CLASSPATH_URL);
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

    /* Update the list of hosts based on cluster members */
    private void updateHosts(){
        /* Get cluster instance */
        Cluster cluster = hazelcast_instance.getCluster();
        
        /* Temporary list of hosts */
        Set<Host> temp_host_list = new HashSet<>();
        
        try{
            /* Get members from cluster */
            Set<Member> members = cluster.getMembers();
            
            /* If members is null, somethin went wrong */
            if(members == null){
                System.err.println("Something went wrong...");
                return;
            }
            
            /* For each member who belong to members list */
            for (Member member : members) {
                /* Create a new host based on cluster member */
                Host host = new Host(new HazelcastNode(member), NODE_CAPACITY);
                
                /* Store the temp host */
                temp_host_list.add(host);
            }
            
            /* Remove all elements of the actual list from the temp list */
            Set<Host> new_hosts = new HashSet<>();
            Set<Host> past_hosts = new HashSet<>();
            
            /* Put all elements in temp list host to discover who notes */
            new_hosts.addAll(temp_host_list);
            new_hosts.removeAll(host_list);
            
            /* Put all elements from old host list, to discover which host had disappear */
            past_hosts.addAll(host_list);
            past_hosts.removeAll(temp_host_list);
            
            /* Check if there are network modifications */
            if(!new_hosts.isEmpty() || !past_hosts.isEmpty()){
                /* Check if there are new hosts on network */
                if(!new_hosts.isEmpty()){
                    /* Add new hosts to host_list */
                    for(Host host : new_hosts){
                        host_list.add(host);
                    }
                }
                
                /* Check if there are some hosts out network */
                if(!past_hosts.isEmpty()){
                    /* Remove those entries from host list */
                    for(Host host : past_hosts){
                        /* Unsubscribe all groups */
                        host.removeAllGroups();
                        
                        /* Remove host from host list */
                        host_list.remove(host);
                    }
                }
                
                /* If there are new nodes or some nodes has exit do balance */
                balanceNetwork();
            }
        }
        catch(Exception e){
            System.err.println("Something went wrong...");
            e.printStackTrace(new PrintStream(System.err));
        }
    }
    
    /**
     * 
     * Balance loading in network.
     * 
     */
    public void balanceNetwork(){
        // TODO
    }
    
    /**
     * 
     * Add a host to a given group.
     * 
     * @param host Host to add.
     * @param group Given group.
     */
    public void addHostToGroup(Host host, Group group){
        /* Add reference of the host to group */
        group.addHost(host);
        
        /* Get group name */
        String group_name = group.getGroupName();
        
        /* Get host node */
        Node node = host.getHostInstance();
        
        /* Add reference of group to host */
        host.addGroup(group_name);
        
        /* Add host to cellar group */
        addHostCellarGroup(node, group_name);
    }
    
    /**
     * 
     * Remove a host from a given group.
     * 
     * @param host Host to remove.
     * @param group Given group.
     */
    public void removeHostFromGroup(Host host, Group group){
        /* Add reference of the host to group */
        group.removeHost(host);
        
        /* Get group name */
        String group_name = group.getGroupName();
        
        /* Get host node */
        Node node = host.getHostInstance();
        
        /* Add reference of group to host */
        host.removeGroup(group_name);
        
        /* Add host to cellar group */
        removeHostCellarGroup(node, group_name);
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
        Set<Node> destination = new HashSet<>();
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
    public void addHostCellarGroup(Node node, String group_name){        
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
    
    /**
     * 
     * Install bundle in a given host.
     * 
     * @param node Given node to install.
     * @param install_urls List of install urls.
     * @param group_name Group name.
     */
    public void hostInstallBundle(Node node, ArrayList<String> install_urls, String group_name){
        /* Get the group based on group name */
        org.apache.karaf.cellar.core.Group group = group_manager.findGroupByName(group_name);
        
        /* If the group is null show error and stop execution */
        if(group == null){
            System.err.println("Cluster group " + group_name + " doesn't exist");
            return;
        }
        
        /* Initialize bundle after install */
        boolean start = true;

        /* Check if the producer is ON, if it's not stop execution */
        if (event_producer.getSwitch().getStatus().equals(SwitchStatus.OFF)) {
            System.err.println("Cluster event producer is OFF");
            return;
        }
        
        /* Create a Cellar Support */
        CellarSupport cellar_support = new CellarSupport();
        cellar_support.setClusterManager(cluster_manager);
        cellar_support.setGroupManager(group_manager);
        cellar_support.setConfigurationAdmin(configuration_admin);      
        
        /* Install a block of maven install urls */
        for (String install_url : install_urls) {
            
            /* Check if the bundle is allowed to install */
            if (cellar_support.isAllowed(group, Constants.CATEGORY, install_url, EventType.OUTBOUND)) {
                /* Jar Input Stream  */
                JarInputStream jar_input_stream;
                
                /* Try retrieve Jar and get manifest */
                try{
                    jar_input_stream = new JarInputStream(new URL(install_url).openStream());
                }
                /* Catch errors of malformed URL exception */
                catch(MalformedURLException e){
                    System.err.println("Something went wrong... Malformed URL!");
                    e.printStackTrace(new PrintStream(System.err));
                    continue;
                }
                /* Catch IO Exception */
                catch(IOException e){
                    System.err.println("Something went wrong... IO Exception!");
                    e.printStackTrace(new PrintStream(System.err));
                    continue;
                }
                
                /* Get the manifest of the jar input stream */
                Manifest manifest = jar_input_stream.getManifest();
                
                /* If the manifest is invalid, skip this bundle */
                if (manifest == null) {
                    System.err.println("Bundle location " + install_url + " doesn't seem correct!");
                    continue;
                }
                
                /* Get Bundle Name */
                String name = manifest.getMainAttributes().getValue("Bundle-Name");
                /* Get Symbolic Name */
                String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
                
                /* Since name cannot be null, we check if name is valid */
                if (name == null) {
                    name = symbolicName;
                }
                
                /* If it's not valid now use install_url as name */
                if (name == null) {
                    name = install_url;
                }
               
                /* Try to get Bundle Version */
                String version;
                
                try{
                    version = manifest.getMainAttributes().getValue("Bundle-Version");
                    jar_input_stream.close();
                }
                catch(IOException e){
                    System.err.println("IO Exception wrong...");
                    e.printStackTrace(new PrintStream(System.err));
                    continue;
                }
                
                /* Get Classloader */
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

                /* Now update the cluster group */
                try {
                    Map<String, BundleState> clusterBundles = cluster_manager.getMap(Constants.BUNDLE_MAP + Configurations.SEPARATOR + group_name);
                    BundleState state = new BundleState();
                    state.setName(name);
                    state.setSymbolicName(symbolicName);
                    state.setVersion(version);
                    state.setId(clusterBundles.size());
                    state.setLocation(install_url);
                    
                    /* Start bundle if it's specified */
                    if (start) {
                        state.setStatus(BundleEvent.STARTED);
                    } else {
                        state.setStatus(BundleEvent.INSTALLED);
                    }

                    System.out.println("status " + state.getStatus());
                    clusterBundles.put(symbolicName + "/" + version, state);
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }

                /* Now broadcast  */
                ClusterBundleEvent event = new ClusterBundleEvent(symbolicName, version, install_url, BundleEvent.INSTALLED);
                event.setSourceGroup(group);
                
                /* Start bundle if it's specified */
                if (start) {
                    event = new ClusterBundleEvent(symbolicName, version, install_url, BundleEvent.STARTED);
                    event.setSourceGroup(group);
                }
                
                System.out.println("event: " + event);
                event_producer.produce(event);

            } else {
                System.err.println("Bundle location " + install_url + " is blocked outbound for cluster group " + group_name);
            }
        }
    }    
   
    /**
     * 
     * Get actual balance score.
     * 
     * @return Actual balance score.
     */
    @Override
    public HardSoftScore getScore() {
        return balance_score;
    }

    /**
     * Set a new balance score.
     * 
     * @param score New balance score.
     */
    @Override
    public void setScore(HardSoftScore score) {
        this.balance_score = score;
    }
    
    /**
     * 
     * This is needed by solver.
     * 
     * @return Collection of facts to be used in solver
     */
    @Override
    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<>();
        facts.addAll(host_list);
        
        // Do not add the planning entity's (processList) because that will be done automatically
        return facts;
    }
    
    
    @ValueRangeProvider(id = "hostRange")
    
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
