/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balance;

import org.apache.karaf.cellar.bundle.BundleState;
import org.apache.karaf.cellar.bundle.Constants;
import org.apache.karaf.cellar.core.event.EventType;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.karaf.cellar.core.ClusterManager;
import org.apache.karaf.cellar.core.Group;
import org.apache.karaf.cellar.core.GroupManager;
import org.apache.karaf.cellar.core.Node;
import org.apache.karaf.cellar.core.command.ExecutionContext;
import org.apache.karaf.cellar.core.control.ManageGroupAction;
import org.apache.karaf.cellar.core.control.ManageGroupCommand;
import org.apache.karaf.cellar.core.control.ManageGroupResult;
import org.apache.karaf.cellar.hazelcast.HazelcastNode;
import org.apache.karaf.shell.support.table.ShellTable;
import org.apache.karaf.cellar.core.shell.CellarCommandSupport;
import org.apache.karaf.cellar.core.CellarSupport;
import org.apache.karaf.cellar.core.control.SwitchStatus;
import java.util.jar.JarInputStream;
import java.net.URL;
import java.util.jar.Manifest;
import org.osgi.framework.BundleEvent;
import org.apache.karaf.cellar.bundle.ClusterBundleEvent;
import org.apache.karaf.cellar.core.Configurations;
import org.apache.karaf.cellar.core.event.EventProducer;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.osgi.service.cm.ConfigurationAdmin;

/*
 * @author jurandir
 */

public class Controller extends CellarCommandSupport {

    private HazelcastInstance instance = null;
    private ExecutionContext executionContext;
    private Map<String, List<String>> bundleGroup;
    private DataSource dataSource;
    private Connection dbConnection;

    @Reference
    protected ClusterManager clusterManager;
    @Reference
    protected GroupManager groupManager;
    @Reference
    protected ConfigurationAdmin configurationAdmin;

    String groupName;
    List<String> urls = new ArrayList<String>();
    List<Long> ids = new ArrayList<Long>();
    List<String> bundles;
    List<String> groups;
    boolean start;
    private EventProducer eventProducer;
    private HashMap<String, List<String>> listHostByGroup = new HashMap<String, List<String>>();

    public void init() {

        try {
            System.out.println("\nGetting the balance management...\n");
            List<String> bundlesInit;    
            String raiz = "file:///G://bundles-iot/";
            
            verifyNodesChanges();
            startGroupBundles();

            String nameBundle="";
            System.out.println("\nDistributing bundles between groups/clusters...\n");
            System.out.println(groups.size());
            for (String g : groups){    
                groupName = g;
                System.out.println(groupName);
                bundlesInit = this.bundleGroup.get(g);
                 for (String b : bundlesInit ){
                    nameBundle = b;
                    System.out.println(nameBundle);
                    urls.add(raiz + nameBundle);
                    installBundles();          
                }
            }
            System.out.println("\n---------------IoT balanced network---------------\n");

            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGroupBundles() {
        
        groups = new ArrayList<String>();
       
        groups.add("composition");
        groups.add("localization");
        groups.add("security");
        groups.add("discovery");
//        groups.add("storage");
//        groups.add("management");

        this.bundleGroup = new HashMap<String, List<String>>();
        
        bundles = new ArrayList<String>();
        bundles.add("app1_com-fatorial-1.0.jar");
        bundles.add("app2_com-fatorial-1.0.jar");
//        bundles.add("app3_com-fatorial-1.0.jar");
//        bundles.add("app4_com-fatorial-1.0.jar");
        this.bundleGroup.put("composition", bundles);

           bundles = new ArrayList<String>();
//         bundles.add("");
//         bundleGroup.put("default", bundles);

        bundles = new ArrayList<String>();
        bundles.add("app1_dis-fatorial-1.0.jar");
        bundles.add("app2_dis-fatorial-1.0.jar");
        bundles.add("app3_dis-fatorial-1.0.jar");
//        bundles.add("app4_dis-fatorial-1.0.jar");
        this.bundleGroup.put("discovery", bundles);

        bundles = new ArrayList<String>();
        bundles.add("app1_loc-fatorial-1.0.jar");
        bundles.add("app2_loc-fatorial-1.0.jar");
//        bundles.add("app3_loc-fatorial-1.0.jar");
//        bundles.add("app4_loc-fatorial-1.0.jar");
        this.bundleGroup.put("localization", bundles);

        bundles = new ArrayList<String>();
        bundles.add("app1_sec-fatorial-1.0.jar");
        bundles.add("app2_sec-fatorial-1.0.jar");
//        bundles.add("app3_sec-fatorial-1.0.jar");
        this.bundleGroup.put("security", bundles);

//        //Está faltando criar o bundle desse grupo management
//        bundles = new ArrayList<String>();
//        bundles.add("app1_man-fatorial-1.0.jar");
//        this.bundleGroup.put("management", bundles);

//        //Está faltando criar o bundle desse grupo storage
//        bundles = new ArrayList<String>();
//        bundles.add("app1_sto-fatorial-1.0.jar");
//        this.bundleGroup.put("storage", bundles);

    }

    public void installBundles() throws IOException {

        System.out.println("doExecute()");
        start = true;

        // check if the group exists
        Group group = groupManager.findGroupByName(groupName);
        if (group == null) {
            System.err.println("Cluster group " + groupName + " doesn't exist");

        }

        // check if the producer is ON
        if (eventProducer.getSwitch().getStatus().equals(SwitchStatus.OFF)) {
            System.err.println("Cluster event producer is OFF");

        }

        CellarSupport support = new CellarSupport();
        support.setClusterManager(this.clusterManager);
        support.setGroupManager(this.groupManager);
        support.setConfigurationAdmin(this.configurationAdmin);

        for (String url : urls) {
            // check if the bundle is allowed
            if (support.isAllowed(group, Constants.CATEGORY, url, EventType.OUTBOUND)) {

                // get the name and version in the location MANIFEST
                JarInputStream jarInputStream = new JarInputStream(new URL(url).openStream());
                Manifest manifest = jarInputStream.getManifest();
                if (manifest == null) {
                    System.err.println("Bundle location " + url + " doesn't seem correct");
                    continue;
                }
                String name = manifest.getMainAttributes().getValue("Bundle-Name");
                String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
                if (name == null) {
                    name = symbolicName;
                }
                if (name == null) {
                    name = url;
                }
                String version = manifest.getMainAttributes().getValue("Bundle-Version");
                jarInputStream.close();

                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

                try {
                    // update the cluster group
                    Map<String, BundleState> clusterBundles = clusterManager.getMap(Constants.BUNDLE_MAP + Configurations.SEPARATOR + groupName);
                    BundleState state = new BundleState();
                    state.setName(name);
                    state.setSymbolicName(symbolicName);
                    state.setVersion(version);
                    state.setId(clusterBundles.size());
                    ids.add(state.getId());
                    state.setLocation(url);
                    if (start) {
                        
                        state.setStatus(BundleEvent.LAZY_ACTIVATION);
                    } else {
                        state.setStatus(BundleEvent.INSTALLED);
                    }
                    
                    System.out.println("status " + state.getStatus());
                    clusterBundles.put(symbolicName + "/" + version, state);
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }

                // broadcast the cluster event
                ClusterBundleEvent event = new ClusterBundleEvent(symbolicName, version, url, BundleEvent.STARTED);
                event.setSourceGroup(group);
                if (start) {
                    event = new ClusterBundleEvent(symbolicName, version, url, BundleEvent.STARTED);
                    event.setSourceGroup(group);
                }
                System.out.println("event: " + event);
                eventProducer.produce(event);

            } else {
                System.err.println("Bundle location " + url + " is blocked outbound for cluster group " + groupName);
            }
        }

    }

    private void nodesServices() {
        NodeService nodeService = new NodeService();

    }

    private void comparativeTable() throws Exception {
        ArrayList<String> ipListCellar = new ArrayList<String>();
        ArrayList<String> ipListTable = new ArrayList();
        ArrayList<String> ipListTemp = new ArrayList();
        ArrayList<String> cellarTemp = new ArrayList();

        // [ B ] usando o método add() para gravar 5 números de IP
        ipListTable.add("192.168.177.1");
        //ipListTable.add("192.168.177.2");
        System.out.println(Arrays.toString(ipListTable.toArray()));

        // Check if there's a instance of hazelcast
        if (instance == null) {
            System.out.println("Instance null.");
            return;
        }

        // Get Cluster with the hazelcast instance
        Cluster cluster = instance.getCluster();

        // Get all members of the Hazelcast Cluster and display some properties
        try {
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {
                for (Member member : members) {
                    HazelcastNode node = new HazelcastNode(member);

                    ipListCellar.add(node.getHost());
                    System.out.println(Arrays.toString(ipListCellar.toArray()));
                    //System.out.println("OBJ = " + node.toString());

                }

//                System.out.println(">>>>"+ ipListTable.equals(ipListCellar));
                ipListTemp.addAll(ipListTable);
                //retorna os gateways que cairam
                ipListTemp.removeAll(ipListCellar);
                //retorna os gateways novos que surgiram
                cellarTemp.removeAll(ipListTable);
                // atualização da lista principal
                // adiciona novos gateways
                ipListTable.addAll(cellarTemp);
                // retirando gateways que sairam do sistema

                System.out.println("ipListTemp" + Arrays.toString(ipListTemp.toArray()));
                System.out.println("cellarTemp" + Arrays.toString(cellarTemp.toArray()));
                System.out.println("ipListTable" + Arrays.toString(ipListTable.toArray()));
                System.out.println("ipListCellar" + Arrays.toString(ipListCellar.toArray()));

                if ((!ipListTemp.isEmpty()) || (!cellarTemp.isEmpty())) {

                    this.createFoTgroups();
                    this.verifyNodesChanges();

                }

            }
        } catch (NullPointerException ex) {
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void createFoTgroups() {
        if (!getGroupManager().isLocalGroup("discovery")) {
            getGroupManager().createGroup("discovery");
        }
        if (!getGroupManager().isLocalGroup("composition")) {
            getGroupManager().createGroup("composition");
        }
        if (!getGroupManager().isLocalGroup("security")) {
            getGroupManager().createGroup("security");
        }
        if (!getGroupManager().isLocalGroup("storage")) {
            getGroupManager().createGroup("storage");
        }
        if (!getGroupManager().isLocalGroup("localization")) {
            getGroupManager().createGroup("localization");
        }
        if (!getGroupManager().isLocalGroup("management")) {
            getGroupManager().createGroup("management");
        }

    }

    private void verifyNodesChanges() throws Exception {
        boolean change = true;
        if (change) {
//            verifyNodesChanges();
            System.out.println("\nRemoving nodes from groups...\n");
            removeNodesGroup();
            System.out.println("\nCreating cluster and inserting nodes...\n");
            balance();
            System.out.println("End of balancing!!!");
        }
    }

    private Set<Node> listNode() {
        Cluster cluster = instance.getCluster();
        Set<Node> listNode = new HashSet<Node>();
        try {
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {
                for (Member member : members) {
                    HazelcastNode node = new HazelcastNode(member);
                    listNode.add(node);
                }
            }
            return listNode;
        } catch (NullPointerException ex) {
            System.out.println("Erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void removeNodesGroup() throws Exception {

        ArrayList<String> info = new ArrayList<String>();
        // Get Cluster with the hazelcast instance
        Cluster cluster = instance.getCluster();

        try {
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {

                //group.unRegisterGroup(groupName) - método utilizado
                getGroupManager().unRegisterGroup("discovery");
                getGroupManager().unRegisterGroup("composition");
                getGroupManager().unRegisterGroup("security");
                getGroupManager().unRegisterGroup("storage");
                getGroupManager().unRegisterGroup("localization");
                getGroupManager().unRegisterGroup("management");

                for (Member member : members) {
                    HazelcastNode node = new HazelcastNode(member);
//                    configuration.getHazelcastConfig();
                    info.add(node.getHost());
                    System.out.println(Arrays.toString(info.toArray()));
                }
            }

//            Set<Node> nos = new HashSet<Node>();
//            for (Member member : members) {
//                HazelcastNode node = new HazelcastNode(member);
//                nos.add(node);
//            }
        } catch (NullPointerException ex) {
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void balance() throws Exception {

        Cluster c = instance.getCluster();
        Set<Node> nodes = new HashSet<Node>();
        Set<Member> members = c.getMembers();
        List<String> listHost;

        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            nodes.add(node);
        }

        ArrayList<String> services = new ArrayList<String>();
        Map<String, Integer> serviceCost = new HashMap<String, Integer>();
        //String[] nome = {"discovery", "composition", "security", "storage", "localization", "management"};
        services.add("discovery");
        serviceCost.put("discovery", 2);
        services.add("composition");
        serviceCost.put("composition", 1);
        services.add("security");
        serviceCost.put("security", 3);
        services.add("storage");
        serviceCost.put("storage", 1);
        services.add("localization");
        serviceCost.put("localization", 3);
        services.add("management");
        serviceCost.put("management", 2);

        ArrayList<Node> hosts = new ArrayList<Node>();
        Map<String, Integer> nodeCapacity = new HashMap<String, Integer>();
        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            hosts.add(node);
            //a linha de baixo deve ser substituida pela a capacidade de cada nó
            //gerada por Nilson
            nodeCapacity.put(node.getHost(), 6);
        }

        int n = 0;
        for (int s = 0; s < services.size(); s++) { //foi removido o "<=" e substituido por "<"
            int NumUncapacityNodes = 0;
            if (n >= hosts.size()) {
                n = 0;
            }
            boolean serviceAllocated = false;
            while (NumUncapacityNodes < hosts.size() && !serviceAllocated) {
                if (nodeCapacity.get(hosts.get(n).getHost()) >= serviceCost.get(services.get(s))) {                    
                    setCellarGroup(services.get(s), hosts.get(n));
                    String host = hosts.get(n).getHost();
                    if ((!listHostByGroup.isEmpty()) && listHostByGroup.containsKey(services.get(s))) {
                        for (String key : listHostByGroup.keySet()) {
                            //Capturamos o valor a partir da chave
                            if (services.get(s).equals(key)) {
                                List<String> value = listHostByGroup.get(key);
                                value.add(host);
                                listHostByGroup.put(key, value);
                            }
                        }
                    } else {
                        listHost = new ArrayList<String>();
                        listHost.add(host);
                        String g = services.get(s);
                        System.out.println("Nome do grupo adicionado ao nó: " + g);
                        listHostByGroup.put(services.get(s), listHost);
                    }
                    impress(); //impressão após cada incremento
                    int newCapacity = nodeCapacity.get(hosts.get(n).getHost()) - serviceCost.get(services.get(s));
                    nodeCapacity.put(hosts.get(n).getHost(), newCapacity);
                    serviceAllocated = true;
                } else {
                    NumUncapacityNodes++;
                }
                n++;
            }
            n-=1;
            if (NumUncapacityNodes >= hosts.size()) {
                Node greaterNode = greaterCapacity(hosts, nodeCapacity);
                setCellarGroup(services.get(s), greaterNode);
                int newCapacity = nodeCapacity.get(greaterNode.getHost()) - serviceCost.get(services.get(s));
                //if(n < hosts.size()){
                    nodeCapacity.put(hosts.get(n).getHost(), newCapacity);
                //}
                
                String host = hosts.get(n).getHost();
                    if ((!listHostByGroup.isEmpty()) && listHostByGroup.containsKey(services.get(s))) {
                        for (String key : listHostByGroup.keySet()) {
                            //Capturamos o valor a partir da chave
                            if (services.get(s).equals(key)) {
                                List<String> value = listHostByGroup.get(key);
                                value.add(host);
                                listHostByGroup.put(key, value);
                            }
                        }
                    } else {
                        listHost = new ArrayList<String>();
                        listHost.add(host);
                        String g = services.get(s);
                        System.out.println(">>>>>>>>>>>>>>>>>>Adicionando novo grupo e seu nó: " + g);
                        listHostByGroup.put(services.get(s), listHost);
                    }
                    impress(); //impressão após cada incremento                
            }
            
        }
    }
    
    private void impress(){ //apenas para impressão
        if (listHostByGroup.isEmpty()) {
            System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>ListHostByGroup vazio.\n\n");
        } else {
            
            System.out.println("Total of Groups: " + listHostByGroup.size());
            for (String key : listHostByGroup.keySet()) {
                //Capturamos o valor a partir da chave
                List<String> value = listHostByGroup.get(key);
                System.out.println("Total of Nodes: " + value.size());
                System.out.println("Group Name = " + key);
                for (String h : value) {
                    System.out.println("Node/Host: " + h);
                }
            }
            System.out.println("\n\n");
        }
    }

    private Node greaterCapacity(ArrayList<Node> nodes, Map<String, Integer> nodeCapacity) {
        Node node = nodes.get(0);
        for (int i = 0; i < nodes.size(); i++) { //foi removido o "<=" e substituido por "<"
            if (nodeCapacity.get(nodes.get(i).getHost()) > nodeCapacity.get(node.getHost())) {
                node = nodes.get(i);
            }
        }
        return node;
    }

    private void setCellarGroup(String group, Node node) throws Exception {
        Set<Node> ip = new HashSet<Node>();
        ip.add(node);
//            ManageGroupCommand command = new ManageGroupCommand(this.cluster.generateId());
        ManageGroupCommand command = new ManageGroupCommand(this.getClusterManager().generateId());
        command.setDestination(ip);
        command.setAction(ManageGroupAction.JOIN);
        command.setGroupName(group);
        //command.setGroupName(group);
        command.setSourceGroup(null);

        Map<Node, ManageGroupResult> results = executionContext.execute(command);

        if (results == null || results.isEmpty()) {
            System.out.println("No result received within given timeout");
        } else {
            ShellTable table = new ShellTable();
            table.column(" ");
            table.column("Group");
            table.column("Members");

            for (Node n : results.keySet()) {
                ManageGroupResult result = results.get(node);
                if (result != null && result.getGroups() != null) {
                    for (Group g : result.getGroups()) {
                        StringBuffer buffer = new StringBuffer();
                        if (g.getNodes() != null && !g.getNodes().isEmpty()) {
                            String local = "";
                            for (Node member : g.getNodes()) {
                                // display only up and running nodes in the cluster
                                //if (this.cluster.findNodeById(member.getId()) != null) {
                                if (this.getClusterManager().findNodeById(member.getId()) != null) {
                                    buffer.append(member.getId());
                                    //if (member.equals(this.cluster.getNode())) {
                                    if (member.equals(this.getClusterManager().getNode())) {
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
        }
        System.out.println("End of nodes grouping!");        
    }

    public void setInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected Object doExecute() throws Exception {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EventProducer getEventProducer() {
        return eventProducer;
    }

    public void setEventProducer(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public ConfigurationAdmin getConfigurationAdmin() {
        return configurationAdmin;
    }

    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

}
