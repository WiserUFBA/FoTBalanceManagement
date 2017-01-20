/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balanceold;

import org.apache.karaf.cellar.bundle.BundleState;
import org.apache.karaf.cellar.bundle.Constants;
import org.apache.karaf.cellar.core.event.EventType;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
    private int bundleGroup;
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

    ArrayList<Node> hosts = new ArrayList<Node>();
    Map<String, Integer> nodeCapacity = new HashMap<String, Integer>();

    ArrayList<String> services = new ArrayList<String>();
    Map<String, Integer> serviceCost = new HashMap<String, Integer>();

    boolean start;
    private EventProducer eventProducer;
    private HashMap<String, List<String>> listHostByGroup = new HashMap<String, List<String>>();

    public void init() {
        //Método init para começão a iniciar os parâmetros do balanceamento
        System.out.println("\nMétodo init\n");
        
        try {
            System.out.println("\nGetting the balance management. Wait...");
            System.out.println("Analyzing nodes with Karaf Cellar... ");

            //Pegando tempo inicial de execução
            Calendar cal = Calendar.getInstance();

            createFoTgroups();
            comparativeTable();

            //Pegando tempo final de execução
            Calendar cal2 = Calendar.getInstance();
            Date d = cal2.getTime();
            System.out.println("\nCycle executed in " + ((cal2.getTimeInMillis() - cal.getTimeInMillis())) + " ms");

            System.out.println("\n------------------IoT balanced network------------------\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @deprecated 
     */
    private void createFoTgroups() {
        //Método createFoTgroups verifica a existencia de cada grupo, caso não exista,
        //o grupo faltante é criado no momento
        System.out.println("\nMétodo createFoTgroups\n");
        
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
        
    /**
     * @deprecated 
     * @throws Exception 
     */
    private void comparativeTable() throws Exception {
        //Método comparativeTable compara uma tabela de nós (futuramente extraida de um bundle específico)
        //com os nós existentes no momento no cluster do Karaf Cellar
        System.out.println("\nMétodo comparativeTable\n");
        
        ArrayList<String> ipListCellar = new ArrayList<String>();
        ArrayList<String> ipListTable = new ArrayList();
        ArrayList<String> ipListTemp = new ArrayList();
        ArrayList<String> cellarTemp = new ArrayList();

        // [ B ] usando o método add() para gravar 'x' números de IP
        ipListTable.add("192.168.0.103");
        ipListTable.add("192.168.0.144");
        ipListTable.add("192.168.0.148");
        ipListTable.add("192.168.0.149");
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

                    System.out.println("Divergent network. New network balancing!");
                    this.createFoTgroups();
                    this.verifyNodesChanges();
                    this.startGroupBundles();
                    this.nodeAllocation();
                    
                } else {
                    System.out.println("Stable network! No balancing!");
                }

            }
        } catch (NullPointerException ex) {
            System.out.println("Erro NullPointerException ex in comparativeTable.");
            //Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * @deprecated 
     * @throws Exception 
     */
    private void verifyNodesChanges() throws Exception {
        //Método verifyNodesChanges inicia o processo de limpeza dos nós nos grupos, monta todo o cluster
        //para iniciar o processo de balanceamento da rede
        System.out.println("\nMétodo verifyNodesChanges\n");
        
        boolean change = true;
        if (change) {
//            verifyNodesChanges();
            System.out.println("\nStep 1/2 - Removing nodes from groups...\n");
            removeNodesGroup();
            System.out.println("\nStep 2/2 - Creating cluster and inserting nodes...\n");
            balance();
            System.out.println("End of the distribution of the groups in the nodes!");
        }
    }
    
    /**
     * @deprecated 
     * @throws Exception 
     */
    private void removeNodesGroup() throws Exception {
        //Método removeNodesGroup exclui todos os nós de seus respectivos grupos
        System.out.println("\nMétodo removeNodesGroup\n");
        
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
            System.out.println("Erro NullPointerException ex in removeNodesGroup().");
            //Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void balance() throws Exception {
        //Método balance distribui os grupos (services) em seus respectivos nós da rede
        System.out.println("\nMétodo balance\n");
        
        Cluster c = instance.getCluster();
        Set<Node> nodes = new HashSet<Node>();
        Set<Member> members = c.getMembers();

        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            nodes.add(node);
        }
        
        //HashMap que distribui o peso (distribuição manual aleatória) existente em cada grupo
        //String[] nome = {"discovery", "composition", "security", "storage", "localization", "management"};
        services.add("discovery");
        serviceCost.put("discovery", 2);
        services.add("localization");
        serviceCost.put("localization", 3);
        services.add("composition");
        serviceCost.put("composition", 1);
        services.add("security");
        serviceCost.put("security", 3);
        services.add("storage");
        serviceCost.put("storage", 1);
        services.add("management");
        serviceCost.put("management", 2);

//        hosts.clear();
        
        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            hosts.add(node);
            //a linha de baixo deve ser substituida pela a capacidade de cada nó
            //gerada por Nilson
            nodeCapacity.put(node.getHost(), 6);
        }

        if (hosts.size() < 3) {
            priorityHosts();
        } else {
            sequenceHosts();
        }
    }

    public void priorityHosts() throws Exception {
        //Método priorityHosts executado quando o número de nós na rede for menor (igual) a 2
        System.out.println("\nMétodo priorityHosts\n");
        
        List<String> listHost;

        int n = 0;
        for (int s = 0; s < 2; s++) { //foi removido o "<=" e substituido por "<"
            int NumUncapacityNodes = 0;
            if (n >= hosts.size()) {
                n = 0;
            }
            boolean serviceAllocated = false;
            while (NumUncapacityNodes < hosts.size() && !serviceAllocated) {
                if (n >= hosts.size()) {
                    n = 0;
                }
                if (nodeCapacity.get(hosts.get(n).getHost()) >= serviceCost.get(services.get(s))) {
                    setCellarGroup(services.get(s), hosts.get(n));
                    String host = hosts.get(n).getHost();
                    //rotina que acrescenta um host a lista de grupo existente (s)
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
                        System.out.println("Group added to the node: " + g);
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
            n -= 1;
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
                    System.out.println("Adding a new group and its node: " + g);
                    listHostByGroup.put(services.get(s), listHost);
                }
                impress(); //impressão após cada incremento                
            }
        }
    }

    public void sequenceHosts() throws Exception {
        //Método sequenceHosts executado quando o número de nós na rede for maior (igual) a 3
        System.out.println("\nMétodo sequenceHosts\n");
        
        List<String> listHost;

        int n = 0;
        for (int s = 0; s < services.size(); s++) { //foi removido o "<=" e substituido por "<"
            int NumUncapacityNodes = 0;
            if (n >= hosts.size()) {
                n = 0;
            }
            boolean serviceAllocated = false;
            while (NumUncapacityNodes < hosts.size() && !serviceAllocated) {
                if (n >= hosts.size()) {
                    n = 0;
                }
                
                if (nodeCapacity.get(hosts.get(n).getHost()) >= serviceCost.get(services.get(s))) {
                    setCellarGroup(services.get(s), hosts.get(n));
                    String host = hosts.get(n).getHost();
                    //rotina que acrescenta um host a lista de grupo existente (s)
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
                        System.out.println("Group added to the node: " + g);
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
            n -= 1;
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
                    System.out.println("Adding a new group and its node: " + g);
                    listHostByGroup.put(services.get(s), listHost);
                }
                impress(); //impressão após cada incremento  
                
            }
        }
//        if(hosts.size() > 2){
//            List<String> bundlesInit;
//            String raiz = "url:http://";
//            String addmvn = ":8181/bundleInstall?mvn=br.ufba.dcc.wiser/";
//            String nameBundle = "";
//            System.out.println("\nDistributing bundles between groups/clusters...\n");
//
//            for (String g : listHostByGroup.keySet()) {
//                groupName = g;
//                System.out.println(groupName);
//                bundlesInit = this.bundleGroup.get(g);
//
//                List<String> nos_grupo = listHostByGroup.get(g);
//
//                for(int z = 0; z < hosts.size() ; z++){
//                    if(nos_grupo.contains(hosts.get(z).getHost())){
//                        for (String b : bundlesInit) {
//                            System.out.println(hosts.size());
//                            System.out.println(hosts.get(z));
//                            String ip_st = hosts.get(z).toString();
//                            String[] parts = ip_st.split("=");
//                            String part1 = parts[1];
//                            String[] parts2 = part1.split(":");
//                            String part2 = parts2[0]; // ip
//                            nameBundle = b;
//                            System.out.println(nameBundle + " install in" + part2);
//                            urls.add(raiz + part2 + addmvn + nameBundle);
//                        }
//                    }
//                }
//            }
//
//            installBundles();
//            urls.clear();
//        }
    }
    
    /**
     * @deprecated 
     */
    private void impress() {
        //Método impress usado apenas para impressão em tela da distribuição executada
        System.out.println("\nMétodo impress\n");
        
        if (listHostByGroup.isEmpty()) {
            System.out.println("\n\n>>>>>>>>>>>>>>> ListHostByGroup vazio <<<<<<<<<<<<<<\n\n");
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
    
    /**
     * @deprecated 
     */
    public void startGroupBundles() {
        //Método starGroupBundles cria o HashMap da relação entre os grupos e seus respectivos bundles
        System.out.println("\nMétodo startGroupBundles\n");

        groups = new ArrayList<String>();

        groups.add("localization");
        groups.add("discovery");
        groups.add("composition");
        groups.add("security");
//        groups.add("storage");
//        groups.add("management");

        this.bundleGroup = new HashMap<String, List<String>>();

        bundles = new ArrayList<String>();
        bundles.add("app1_dis-fatorial/1.0");
        bundles.add("app2_dis-fatorial/1.0");
        bundles.add("app3_dis-fatorial/1.0");
        bundles.add("app4_dis-fatorial/1.0");
        this.bundleGroup.put("discovery", bundles);

        bundles = new ArrayList<String>();
        bundles.add("app1_loc-fatorial/1.0");
        bundles.add("app2_loc-fatorial/1.0");
        bundles.add("app3_loc-fatorial/1.0");
        bundles.add("app4_loc-fatorial/1.0");
        this.bundleGroup.put("localization", bundles);

        bundles = new ArrayList<String>();
        bundles.add("app1_sec-fatorial/1.0");
        bundles.add("app2_sec-fatorial/1.0");
        bundles.add("app3_sec-fatorial/1.0");
        this.bundleGroup.put("security", bundles);

        bundles = new ArrayList<String>();
        bundles.add("app1_com-fatorial/1.0");
        bundles.add("app2_com-fatorial/1.0");
        bundles.add("app3_com-fatorial/1.0");
        bundles.add("app4_com-fatorial/1.0");
        this.bundleGroup.put("composition", bundles);

        bundles = new ArrayList<String>();
//         bundles.add("");
//         bundleGroup.put("default", bundles);

//        //Está faltando criar o bundle desse grupo management
//        bundles = new ArrayList<String>();
//        bundles.add("app1_man-fatorial-1.0.jar");
//        this.bundleGroup.put("management", bundles);
//        //Está faltando criar o bundle desse grupo storage
//        bundles = new ArrayList<String>();
//        bundles.add("app1_sto-fatorial-1.0.jar");
//        this.bundleGroup.put("storage", bundles);
    }
    
    public void nodeAllocation() throws IOException {
        //Método nodeAllocation aloca os nó com suas respectivas prioridades
        //menor igual a dois nós ou maior que dois nós
        System.out.println("\nMétodo nodeAllocation\n");

//            String raiz = "file:///G://bundles-iot/";
        List<String> bundlesInit;
        String raiz = "url:http://";
        String addmvn = ":8181/bundleInstall?mvn=br.ufba.dcc.wiser/";
        String nameBundle = "";
        System.out.println("\nDistributing bundles between groups/clusters...\n");

        if (hosts.size() <= 2) {
            System.out.println(groups.size());
            if (hosts.size() == 1) {
                for (int p = 0; p < 2; p++) {
                    groupName = groups.get(p);
                    System.out.println(groupName);
                    bundlesInit = this.bundleGroup.get(groups.get(p));
                    for (String b : bundlesInit) {
                        System.out.println(hosts.get(0));
                        String ip_st = hosts.get(0).toString();
                        String[] parts = ip_st.split("=");
                        String part1 = parts[1];
                        String[] parts2 = part1.split(":");
                        String part2 = parts2[0]; // ip
                        nameBundle = b;
                        System.out.println(nameBundle + " install in " + part2);
                        urls.add(raiz + part2 + addmvn + nameBundle);
                        this.installBundles();
                    }
                }
            } else {
                for (int p = 0; p < 2; p++) {
                    groupName = groups.get(p);
                    System.out.println(groupName);
                    bundlesInit = this.bundleGroup.get(groups.get(p));
                    for (String b : bundlesInit) {
                        System.out.println(hosts.get(p));
                        String ip_st = hosts.get(p).toString();
                        String[] parts = ip_st.split("=");
                        String part1 = parts[1];
                        String[] parts2 = part1.split(":");
                        String part2 = parts2[0]; // ip
                        nameBundle = b;
                        System.out.println(nameBundle + " install in " + part2);
                        urls.add(raiz + part2 + addmvn + nameBundle);
                        this.installBundles();
                    }
                }
            }
        } else {
            int z = 0;
            for (String g : groups) {
                groupName = g;
                System.out.println(groupName);
                bundlesInit = this.bundleGroup.get(g);
                for (String b : bundlesInit) {
                    System.out.println(hosts.size());
                    System.out.println(hosts.get(z));
                    String ip_st = hosts.get(z).toString();
                    String[] parts = ip_st.split("=");
                    String part1 = parts[1];
                    String[] parts2 = part1.split(":");
                    String part2 = parts2[0]; // ip
                    nameBundle = b;
                    System.out.println(nameBundle + " install in " + part2);
                    urls.add(raiz + part2 + addmvn + nameBundle);
                    z++;
                    installBundles();
                }
            }
        }
    }

    /**
     * @deprecated 
     * @throws IOException 
     */
    public void installBundles() throws IOException {
        //Método installBundles extraido do código fonte do Karaf Cellar para instalação de bundles
        System.out.println("\nMétodo installBundles\n");
        
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

    /**
     * @deprecated 
     * @param nodes
     * @param nodeCapacity
     * @return 
     */
    private Node greaterCapacity(ArrayList<Node> nodes, Map<String, Integer> nodeCapacity) {
        Node node = nodes.get(0);
        for (int i = 0; i < nodes.size(); i++) { //foi removido o "<=" e substituido por "<"
            if (nodeCapacity.get(nodes.get(i).getHost()) > nodeCapacity.get(node.getHost())) {
                node = nodes.get(i);
            }
        }
        return node;
    }

    /**
     * @deprecated 
     * @param group
     * @param node
     * @throws Exception 
     */
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

//    private Set<Node> listNode() {
//        Cluster cluster = instance.getCluster();
//        Set<Node> listNode = new HashSet<Node>();
//        try {
//            Set<Member> members = cluster.getMembers();
//            if (members != null && !members.isEmpty()) {
//                for (Member member : members) {
//                    HazelcastNode node = new HazelcastNode(member);
//                    listNode.add(node);
//                }
//            }
//            return listNode;
//        } catch (NullPointerException ex) {
//            System.out.println("Erro 2");
//            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }

     // <editor-fold defaultstate="collapsed" desc="Basic Getter and Setter Functions">

    
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

    @Override
    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    @Override
    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public GroupManager getGroupManager() {
        return groupManager;
    }

    @Override
    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public ConfigurationAdmin getConfigurationAdmin() {
        return configurationAdmin;
    }

    @Override
    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }

     // </editor-fold>
    
}
