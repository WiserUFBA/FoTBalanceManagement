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

import br.ufba.dcc.wiser.fot.balance.config.GroupConfigFile;
import br.ufba.dcc.wiser.fot.balance.config.HostConfigFile;
import br.ufba.dcc.wiser.fot.balance.config.HostConfigFileObject;
import br.ufba.dcc.wiser.fot.balance.solver.FoTBalanceIncrementalScoreCalculator;
import br.ufba.dcc.wiser.fot.balance.entity.Bundles;
import br.ufba.dcc.wiser.fot.balance.entity.Host;
import br.ufba.dcc.wiser.fot.balance.entity.Group;
import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.osgi.framework.BundleEvent;
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
    /* Set of Host */
    private final Set<Host> host_list;
    /* Map of Groups */
    private final Map<String, Group> group_list;
    /* List of offline hosts, hosts which will loose bundles after caming online */
    private final Map<Host, Map<String, List<String>>> offline_hosts_to_remove_bundles;

    /* Host Groups Associations */
    private final Map<String, Set<Group>> host_group_associations;

    /* Factory of Group Solver */
    private final SolverConfig solver_config;

    /* Group Solver Class */
    private Solver<Group> solver;

    /* Configuration files */
    private List<HostConfigFileObject> host_configurations;
    private List<Group> group_configurations;

    /* Default Node Capacity */
    public static int NODE_CAPACITY = 6;

    /* Solver configuration file */
    public static final String SOLVER_CONFIGURATION = "br/ufba/dcc/wiser/fot/balance/solver/fotBalanceSolverConfig.xml";

    /* Karaf Install Port, used by bundles */
    public static final int KARAF_INSTALL_PORT = 8181;

    /* Karaf Default Start Level for new bundles */
    public static final int DEFAULT_START_LEVEL = 80;

    /* List of groups for *ONE HOST* case */
    public static final String[] DEFAULT_GROUP_LIST_HOST0 = {"localization", "basic", "discover"};
    public static List<String> GROUPS_LIST_ONEHOSTCASE = new ArrayList<>(Arrays.asList(DEFAULT_GROUP_LIST_HOST0));

    /* List of groups for *TWO HOST* case */
    public static final String[] DEFAULT_GROUP_LIST_HOST1 = {"basic", "discover"};
    public static final String[] DEFAULT_GROUP_LIST_HOST2 = {"basic", "localization"};
    public static List<String> GROUPS_LIST_TWOHOSTCASE_HOST1 = new ArrayList<>(Arrays.asList(DEFAULT_GROUP_LIST_HOST1));
    public static List<String> GROUPS_LIST_TWOHOSTCASE_HOST2 = new ArrayList<>(Arrays.asList(DEFAULT_GROUP_LIST_HOST2));

    /* OptaPlanner Best Score */
    private final static String BEST_SCORE_LIMIT = "0hard/0soft";

    /* OptaPlanner Maximum seconds spent on balancing */
    private final static Long SECONDS_SPENT_LIMIT = new Long(10);

    /* OptaPlanner Maximum number of calculations per balancing */
    private final static Long CALCULATION_COUNT_LIMIT = new Long(100000);

    /**
     *
     * Create a new Controller instance.
     *
     */
    public Controller() {
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

        /* Create the list of offline hosts, which will loose the bundles installed by this controller */
        offline_hosts_to_remove_bundles = new HashMap<>();

        /* OptaPlanner Solver Configurations */
        solver_config = new SolverConfig();

        /* Host and Group associations */
        host_group_associations = new HashMap<>();
    }

    /**
     * Return the instance of FoT Balance Controller.
     *
     * @return A FoT balance controller.
     */
    public static Controller getInstance() {
        /* If the controller is not initialized yet, create a new instance of controller */
        if (instance == null) {
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
    public void init() {
        /* Initializing Controller */
        FoTBalanceUtils.info("Initializing FoT Balance Management Controller");

        /* Create OptaPlanner Solver */
        try {
            /* Load Input stream of solver configuration */
            //InputStream solver_configuration_stream = getClass().getClassLoader().getResourceAsStream(SOLVER_CONFIGURATION);

            /* !!!!!!!!!!!!!!!!!!! This don't work !!!!!!!!!!!!!!!!!!! */
            /* OptaPlanner Solver Factory */
            //solver_factory = SolverFactory.createFromXmlInputStream(solver_configuration_stream, FoTBalanceIncrementalScoreCalculator.class.getClassLoader());
            /* !!!!!!!!!!!!!!!!!!! Ugly but works at all !!!!!!!!!!!!!!!!!!! */
            /* Create a Solver Config Contexts */
            SolverConfigContext solver_config_context = new SolverConfigContext();
            solver_config_context.setClassLoader(FoTBalanceIncrementalScoreCalculator.class.getClassLoader());

            /* Configure Score Director class */
            ScoreDirectorFactoryConfig score_director = new ScoreDirectorFactoryConfig();
            score_director.setIncrementalScoreCalculatorClass(FoTBalanceIncrementalScoreCalculator.class);

            /* Configure Termination Settings */
            TerminationConfig termination_config = new TerminationConfig();
            termination_config.setBestScoreLimit(BEST_SCORE_LIMIT);
            termination_config.setSecondsSpentLimit(SECONDS_SPENT_LIMIT);
            termination_config.setScoreCalculationCountLimit(CALCULATION_COUNT_LIMIT);

            /* Entity Class List */
            List<Class<?>> entity_class_list = new ArrayList();
            entity_class_list.add(Bundles.class);

            /* Store the configuration created on solver config */
            solver_config.setTerminationConfig(termination_config);
            solver_config.setScoreDirectorFactoryConfig(score_director);
            solver_config.setEntityClassList(entity_class_list);

            /* Configure Solution Class */
            solver_config.setSolutionClass(Group.class);

            /* OptaPlanner Solver */
            //solver = solver_factory.buildSolver(); // DON'T WORKS, SO DON'T USE IT
            solver = solver_config.buildSolver(solver_config_context);
        } catch (Exception e) {
            FoTBalanceUtils.error("Cannot load solver configuration or construct solver");
            FoTBalanceUtils.trace(e.getMessage());
        }

        /* Load Configuration Files */
        host_configurations = HostConfigFile.getConfigurationsFromInstance();
        System.out.println("++++++++++++++++HMM111111111111++++++++++++++++");
        group_configurations = GroupConfigFile.getConfigurationsFromInstance();
        System.out.println("++++++++++++++++HMM222222222222++++++++++++++++");
        
        try {
            /* Store groups defined on the configuration file */
            for (Group temp_group : group_configurations) {

                /* Get Group Name */
                String temp_group_name = temp_group.getGroupName();

                /* Add reference to group list */
                group_list.put(temp_group_name, temp_group);

                /* Register the group on cellar */
                FoTBalanceUtils.info("Registering group on cellar");
                createCellarGroup(temp_group_name);
            }
        } catch (Exception e) {
            FoTBalanceUtils.error("Cannot register cellar group");
            e.printStackTrace(); // THIS SHOULD BE REMOVED IT'S ONLY HERE FOR DEBUG PURPOSES
            FoTBalanceUtils.trace(e.getMessage());
        }

        /* Store list of groups by hostname */
        try{
            for (HostConfigFileObject host_config_object : host_configurations) {
                /* List of groups associated with this host */
                Set<Group> groups_associated = new HashSet<>();

                /* For each group associated with this host add a reference to group object */
                for (String group_name : host_config_object.getGroupsList()) {
                    groups_associated.add(group_list.get(group_name));
                }

                /* Finnaly add list of group references to this host */
                host_group_associations.put(host_config_object.getHostname(), groups_associated);
            }
        } catch (Exception e) {
            FoTBalanceUtils.error("Cannot store host groups");
            e.printStackTrace(); // THIS SHOULD BE REMOVED IT'S ONLY HERE FOR DEBUG PURPOSES
            FoTBalanceUtils.trace(e.getMessage());
        }
        
        /* Store this new object in a static reference */
        FoTBalanceUtils.info("Storing new FoT Balance Controller");
        instance = this;

        // TODO, THIS SHOULD MOUNT ALL SYSTEM
    }

    /**
     *
     * Register a new group.
     *
     * @param group_name The name of the group to be registered.
     */
    public void addGroup(String group_name) {
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
    public void removeGroup(String group_name) {
        /* Get the group with the given group name */
        Group group = getGroup(group_name);

        /* Remove all hosts from group */
        group.removeAllHosts();

        /* Unregister this group from cellar */
        removeCellarGroup(group_name);
    }

    /* Update the list of hosts based on cluster members */
    public void updateHosts() {
        /* If some of the interfaces is still not initialized stop this function */
        FoTBalanceUtils.info("--- Updating Hosts ---");

        /* Hazelcast instance don't exist or it's not initialized yet */
        if (hazelcast_instance == null) {
            FoTBalanceUtils.error("Hazelcast instance don't exist or it's not initialized yet");
            return;
        }

        /* Execution context don't exist or it's not initialized yet */
        if (execution_context == null) {
            FoTBalanceUtils.error("Execution context don't exist or it's not initialized yet");
            return;
        }

        /* Event producer don't exist or it's not initialized yet */
        if (event_producer == null) {
            FoTBalanceUtils.error("Event producer don't exist or it's not initialized yet");
            return;
        }

        /* Cluster manager don't exist or it's not initialized yet */
        if (cluster_manager == null) {
            FoTBalanceUtils.error("Cluster manager don't exist or it's not initialized yet");
            return;
        }

        /* Group manager don't exist or it's not initialized yet */
        if (group_manager == null) {
            FoTBalanceUtils.error("Group manager don't exist or it's not initialized yet");
            return;
        }

        /* Configuration admin don't exist or it's not initialized yet */
        if (configuration_admin == null) {
            FoTBalanceUtils.error("Configuration admin don't exist or it's not initialized yet");
            return;
        }

        /* Some of the lists isn't working or it's not initialized yet */
        if ((host_list == null) || (group_list == null) || (offline_hosts_to_remove_bundles == null)) {
            FoTBalanceUtils.error("Some of the lists isn't working or it's not initialized yet");
            return;
        }

        /* Solver config don't exist or it's not initialized yet */
        if (solver_config == null) {
            FoTBalanceUtils.error("Solver Config don't exist or it's not initialized yet");
            return;
        }

        /* Solver don't exist or it's not initialized yet */
        if (solver == null) {
            FoTBalanceUtils.error("Solver don't exist or it's not initialized yet");
            return;
        }

        /* Configuration files not loaded */
        if (host_configurations == null || group_configurations == null) {
            FoTBalanceUtils.error("Configuration files not loaded");
            return;
        }

        /* Since singleton instance of Controller is needed by some classes, we check if this instance is initialized */
        if (instance == null) {
            FoTBalanceUtils.error("Controller Singleton instace don't exist or it's not initialized yet");
            return;
        }

        /* Get cluster instance */
        Cluster cluster = hazelcast_instance.getCluster();

        /* Temporary list of hosts */
        Set<Host> temp_host_list = new HashSet<>();

        try {
            /* Get members from cluster */
            Set<Member> members = cluster.getMembers();

            /* If members is null, something went wrong */
            if (members == null) {
                FoTBalanceUtils.error("Error retrieving members from cluster object");
                return;
            }

            /* For each member who belong to members list */
            for (Member member : members) {
                String hostname_fqdn = "";

                try {
                    /* Get FQDN of this member */
                    hostname_fqdn = member.getAddress().getInetAddress().getHostName();
                } catch (UnknownHostException e) {
                    FoTBalanceUtils.error("Cannot retrieve FQDN of member");
                    FoTBalanceUtils.trace(e.getMessage());
                }

                /* Create a new host based on cluster member */
                Host host = new Host(new HazelcastNode(member), hostname_fqdn, NODE_CAPACITY);

                /* Store the temp host */
                temp_host_list.add(host);
            }

            /* Remove all elements of the actual list from the temp list */
            Set<Host> new_hosts = new HashSet<>();
            Set<Host> past_hosts = new HashSet<>();

            /* Put all elements in temp list host to discover who is new */
            new_hosts.addAll(temp_host_list);
            new_hosts.removeAll(host_list);

            /* Put all elements from old host list, to discover which host had disappear */
            past_hosts.addAll(host_list);
            past_hosts.removeAll(temp_host_list);

            /* Check if there are network modifications */
            if (!new_hosts.isEmpty() || !past_hosts.isEmpty()) {
                /* Check if there are new hosts on network */
                if (!new_hosts.isEmpty()) {
                    /* Add new hosts to host_list */
                    for (Host host : new_hosts) {
                        /* If this host has bundles pendent to remove, remove they first and then add to host list */
                        Map<String, List<String>> bundles_to_remove = offline_hosts_to_remove_bundles.get(host);

                        /* Check if this host has never entered this list or this list is empty */
                        if (bundles_to_remove != null && bundles_to_remove.size() > 0) {
                            /* For each group name unninstal all pendent bundles */
                            for (String group_name : bundles_to_remove.keySet()) {
                                /* List of unninstal urls */
                                List<String> uninstall_urls = bundles_to_remove.get(group_name);

                                /* Unninstall all urls received */
                                hostUnninstalBundle(host.getHostInstance(), uninstall_urls, group_name);
                            }

                            /* Clean list after unninstall all the pendent bundles */
                            bundles_to_remove.clear();
                        }

                        /* Get the groups of this host */
                        Set<Group> groups_associated = host_group_associations.get(host.getHostID());

                        /* Check how many hosts we have since if we have special rules for cases with one and two hosts */
                        if ((host_list.size() + new_hosts.size() - past_hosts.size()) > 2) {
                            /* If we have groups associated with this host */
                            if (groups_associated != null && groups_associated.size() > 0) {

                                /* Register the groups of this host in this instance */
                                for (Group group_associated : groups_associated) {
                                    host.addGroup(group_associated.getGroupName());
                                }
                            }
                        }

                        /* Finnaly add host to host list */
                        host_list.add(host);
                    }
                }

                /* Check if there are some hosts out network */
                if (!past_hosts.isEmpty()) {
                    /* Remove those entries from host list */
                    for (Host host : past_hosts) {
                        /* Add to the list of hosts with bundles to remove, since we can't remove the bundles now */
                        offline_hosts_to_remove_bundles.put(host, host.getAllUninstalUrls());

                        /* Unsubscribe all groups */
                        host.removeAllGroups();

                        /* Remove host from host list */
                        host_list.remove(host);
                    }
                }

                /* If we have only one or only two hosts we have a special case */
                if (host_list.size() <= 2) {

                    /* If we have one or two hosts we need to remove additional groups in order to keep these hosts ok */
                    for (Host host : host_list) {
                        /* Get the map of unninstall urls */
                        Map<String, List<String>> unninstal_urls_groups = host.getAllUninstalUrls();

                        /* Unsubscribe all groups */
                        host.removeAllGroups();

                        /* For each group name unninstal all pendent bundles */
                        for (String group_name : unninstal_urls_groups.keySet()) {
                            /* List of unninstal urls */
                            List<String> uninstall_urls = unninstal_urls_groups.get(group_name);

                            /* Unninstall all urls received */
                            hostUnninstalBundle(host.getHostInstance(), uninstall_urls, group_name);
                        }
                    }

                    /* Register hosts on priority groups */
                    if (host_list.size() == 1) {
                        /* Host 0 -- The default host in one host case */
                        Host host0 = host_list.iterator().next();

                        /* Register host 0 with the priority groups */
                        for (String group_name : DEFAULT_GROUP_LIST_HOST0) {
                            host0.addGroup(group_name);
                        }
                    } else {
                        /* Base iterator */
                        Iterator<Host> host_iterator = host_list.iterator();

                        /* Host 1 -- The first host on two host case */
                        Host host1 = host_iterator.next();

                        /* Host 2 -- The second host on two host case */
                        Host host2 = host_iterator.next();

                        /* Register host 1 with the priority groups */
                        for (String group_name : DEFAULT_GROUP_LIST_HOST1) {
                            host1.addGroup(group_name);
                        }

                        /* Register host 2 with the priority groups */
                        for (String group_name : DEFAULT_GROUP_LIST_HOST1) {
                            host2.addGroup(group_name);
                        }
                    }
                }

                /* Since network has changed we need to balance it again */
                balanceNetwork();
            }
        } catch (Exception e) {
            FoTBalanceUtils.error("Several error on network check");
            e.printStackTrace(); // THIS SHOULD BE REMOVED IT'S ONLY HERE FOR DEBUG PURPOSES
            FoTBalanceUtils.trace(e.getMessage());
        }
    }

    /**
     *
     * Balance loading in network.
     *
     */
    private void balanceNetwork() {
        /* Display the begin of network balancing */
        FoTBalanceUtils.info("----- Begin of Balance -----");

        /* For each Group solve the class, compare results and do the network changes */
        for (String group_name : group_list.keySet()) {
            /* Actual group */
            Group actual_group = group_list.get(group_name);

            /* Print info about actual group */
            FoTBalanceUtils.info("------ Before Balance ------");
            actual_group.displayAssociations();

            /* Balance this group */
            Group solved_group = solver.solve(actual_group);

            /* Print info about new associations */
            FoTBalanceUtils.info("------ After  Balance ------");
            solved_group.displayAssociations();

            // TODO: DO THE CHANGES ON NETWORK
            // TODO: COMPARE IT WITH THE LAST CONFIGURATION
            // TODO: INSTALL AND UNINSTALL PACKAGES
            //TODO
            /* Check associations after balance finish */
            actual_group.checkMapAssociations();
        }

        /* Show that re reach the end of fot balacing */
        FoTBalanceUtils.info("------ End of Balance ------");
    }

    /**
     *
     * Add a host to a given group.
     *
     * @param host Host to add.
     * @param group Given group.
     */
    public void addHostToGroup(Host host, Group group) {
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
    public void removeHostFromGroup(Host host, Group group) {
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
    public Group getGroup(String group_name) {
        return group_list.get(group_name);
    }

    /**
     *
     * Create a cluster group on Cellar.
     *
     * @param group_name The name of the group desired.
     */
    public void createCellarGroup(String group_name) {
        /* Check if the group isn't present on the local host and then create it */
        if (!group_manager.isLocalGroup(group_name)) {
            group_manager.createGroup(group_name);
        }
    }

    /**
     *
     * Remove a given cluster group.
     *
     * @param group_name The name of the group desired.
     */
    public void removeCellarGroup(String group_name) {
        group_manager.deleteGroup(group_name);
    }

    /* Execute a given manage group command */
    private void executeManageGroupCommand(Node node, String group_name, ManageGroupAction manage_action) {
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

        try {
            /* Try to execute the command */
            results = execution_context.execute(command);
        } catch (Exception e) {
            FoTBalanceUtils.error("Something went wrong...");
            FoTBalanceUtils.trace(e.getMessage());
        }

        /* Check result and print table */
        if (results == null || results.isEmpty()) {
            FoTBalanceUtils.warn("No result received within given timeout");
        } else {
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
    public void addHostCellarGroup(Node node, String group_name) {
        executeManageGroupCommand(node, group_name, ManageGroupAction.JOIN);
    }

    /**
     *
     * Remove a host from a given cellar group.
     *
     * @param node Host that will be removed.
     * @param group_name Group name from which node will be removed.
     */
    public void removeHostCellarGroup(Node node, String group_name) {
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
    public void hostInstallBundle(Node node, List<String> install_urls, String group_name) {
        /* Get the group based on group name */
        org.apache.karaf.cellar.core.Group group = group_manager.findGroupByName(group_name);

        /* If the group is null show error and stop execution */
        if (group == null) {
            FoTBalanceUtils.error("Cluster group " + group_name + " doesn't exist");
            return;
        }

        /* Initialize bundle after install */
        boolean start = true;

        /* Check if the producer is ON, if it's not stop execution */
        if (event_producer.getSwitch().getStatus().equals(SwitchStatus.OFF)) {
            FoTBalanceUtils.error("Cluster event producer is OFF");
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
                try {
                    jar_input_stream = new JarInputStream(new URL(install_url).openStream());
                } /* Catch errors of malformed URL exception */ catch (MalformedURLException e) {
                    FoTBalanceUtils.error("Something went wrong... Malformed URL!");
                    FoTBalanceUtils.trace(e.getMessage());
                    continue;
                } /* Catch IO Exception */ catch (IOException e) {
                    FoTBalanceUtils.error("Something went wrong... IO Exception!");
                    FoTBalanceUtils.trace(e.getMessage());
                    continue;
                }

                /* Get the manifest of the jar input stream */
                Manifest manifest = jar_input_stream.getManifest();

                /* If the manifest is invalid, skip this bundle */
                if (manifest == null) {
                    FoTBalanceUtils.error("Bundle location " + install_url + " doesn't seem correct!");
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

                try {
                    version = manifest.getMainAttributes().getValue("Bundle-Version");
                    jar_input_stream.close();
                } catch (IOException e) {
                    FoTBalanceUtils.error("IO Exception wrong...");
                    FoTBalanceUtils.trace(e.getMessage());
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

                    FoTBalanceUtils.info("Bundle " + name + " status " + state.getStatus());
                    clusterBundles.put(symbolicName + "/" + version, state);
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }

                /* Event of bundles of cluster */
                ClusterBundleEvent event;

                /* Start bundle if it's specified */
                if (start) {
                    event = new ClusterBundleEvent(symbolicName, version, install_url, DEFAULT_START_LEVEL, BundleEvent.STARTED);
                    event.setSourceGroup(group);
                } /* Otherwise mark as installed */ else {
                    event = new ClusterBundleEvent(symbolicName, version, install_url, DEFAULT_START_LEVEL, BundleEvent.INSTALLED);
                    event.setSourceGroup(group);
                }

                FoTBalanceUtils.info("Event: " + event);
                event_producer.produce(event);

            } else {
                FoTBalanceUtils.error("Bundle location " + install_url
                        + " is blocked outbound for cluster group " + group_name);
            }
        }
    }

    /**
     *
     * Uninstall a list of bundle in a given host.
     *
     * @param node Given node to uninstall bundles.
     * @param uninstall_urls List of uninstall urls.
     * @param group_name Group name.
     */
    public void hostUnninstalBundle(Node node, List<String> uninstall_urls, String group_name) {
        /* Get the group based on group name */
        org.apache.karaf.cellar.core.Group group = group_manager.findGroupByName(group_name);

        /* If the group is null show error and stop execution */
        if (group == null) {
            FoTBalanceUtils.error("Cluster group " + group_name + " doesn't exist");
            return;
        }

        /* Check if the producer is ON, if it's not stop execution */
        if (event_producer.getSwitch().getStatus().equals(SwitchStatus.OFF)) {
            FoTBalanceUtils.error("Cluster event producer is OFF");
            return;
        }

        /* Create a Cellar Support */
        CellarSupport cellar_support = new CellarSupport();
        cellar_support.setClusterManager(cluster_manager);
        cellar_support.setGroupManager(group_manager);
        cellar_support.setConfigurationAdmin(configuration_admin);

        /* Uninstall a block of maven install urls */
        for (String uninstall_url : uninstall_urls) {

            /* Check if the bundle is allowed to uninstall */
            if (cellar_support.isAllowed(group, Constants.CATEGORY, uninstall_url, EventType.OUTBOUND)) {
                /* Jar Input Stream  */
                JarInputStream jar_input_stream;

                /* Try retrieve Jar and get manifest */
                try {
                    jar_input_stream = new JarInputStream(new URL(uninstall_url).openStream());
                } /* Catch errors of malformed URL exception */ catch (MalformedURLException e) {
                    FoTBalanceUtils.error("Something went wrong... Malformed URL!");
                    FoTBalanceUtils.trace(e.getMessage());
                    continue;
                } /* Catch IO Exception */ catch (IOException e) {
                    FoTBalanceUtils.error("Something went wrong... IO Exception!");
                    FoTBalanceUtils.trace(e.getMessage());
                    continue;
                }

                /* Get the manifest of the jar input stream */
                Manifest manifest = jar_input_stream.getManifest();

                /* If the manifest is invalid, skip this bundle */
                if (manifest == null) {
                    FoTBalanceUtils.error("Bundle location " + uninstall_url + " doesn't seem correct!");
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
                    name = uninstall_url;
                }

                /* Try to get Bundle Version */
                String version;

                try {
                    version = manifest.getMainAttributes().getValue("Bundle-Version");
                    jar_input_stream.close();
                } catch (IOException e) {
                    FoTBalanceUtils.error("IO Exception wrong...");
                    FoTBalanceUtils.trace(e.getMessage());
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
                    state.setLocation(uninstall_url);

                    /* Set this bundle as uninstalled */
                    state.setStatus(BundleEvent.UNINSTALLED);

                    FoTBalanceUtils.info("Bundle " + name + " status " + state.getStatus());
                    clusterBundles.put(symbolicName + "/" + version, state);
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }

                /* Event of bundles of cluster */
                ClusterBundleEvent event;

                /* Set the bundle as uninstalled */
                event = new ClusterBundleEvent(symbolicName, version, uninstall_url, DEFAULT_START_LEVEL, BundleEvent.UNINSTALLED);
                event.setSourceGroup(group);
                FoTBalanceUtils.info("Event: " + event);
                event_producer.produce(event);

            } else {
                FoTBalanceUtils.error("Bundle location " + uninstall_url
                        + " is blocked outbound for cluster group " + group_name);
            }
        }
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
     * Check if there are a group with the specified name.
     *
     * @param group_name Group name which will be tested
     * @return True if this group is already registered and false otherwise.
     */
    public boolean groupExists(String group_name) {
        return group_list.containsKey(group_name);
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
