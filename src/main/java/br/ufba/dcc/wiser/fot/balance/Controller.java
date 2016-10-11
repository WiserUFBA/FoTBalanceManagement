/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balance;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import static java.lang.Compiler.command;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
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

/*
 * @author jurandir
 */
public class Controller {

    private HazelcastInstance instance = null;
    private ClusterManager cluster = null;
    private GroupManager group = null;
    private ExecutionContext executionContext;
    private DataSource dataSource;
    private Connection dbConnection;

    public void init() throws Exception {
        System.out.println("Getting the balance...");
        createFoTgroups();
        startBalance();
        blaBli();
        }

    private void createFoTgroups() {
        if (!group.isLocalGroup("discovery")) {
            group.createGroup("discovery");
        }
        if (!group.isLocalGroup("composition")) {
            group.createGroup("composition");
        }
        if (!group.isLocalGroup("security")) {
            group.createGroup("security");
        }
        if (!group.isLocalGroup("storage")) {
            group.createGroup("storage");
        }
        if (!group.isLocalGroup("localization")) {
            group.createGroup("localization");
        }
        if (!group.isLocalGroup("management")) {
            group.createGroup("management");
        }

    }

    private void verifyNodesChanges() throws Exception {
        //boolean change = true;
        //if (change) {
        startBalance();
        //  }
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

    private void startBalance() throws Exception {

        ArrayList<String> info = new ArrayList<String>();
        // Get Cluster with the hazelcast instance
        Cluster cluster = instance.getCluster();

        try {
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {

                //group.unRegisterGroup(groupName) - método utilizado
                group.unRegisterGroup("discovery");
                group.unRegisterGroup("composition");
                group.unRegisterGroup("security");
                group.unRegisterGroup("storage");
                group.unRegisterGroup("localization");
                group.unRegisterGroup("management");

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

    private void blaBli() throws Exception {

        Cluster c = instance.getCluster();
        Set<Node> nodes = new HashSet<Node>();
        Set<Member> members = c.getMembers();

        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            nodes.add(node);
        }

        ArrayList<String> services = new ArrayList<String>();
        Map<String,Integer> serviceCost = new HashMap<String,Integer>();
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
        Map<String,Integer> nodeCapacity = new HashMap<String,Integer>();
        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            hosts.add(node);
            //a linha de baixo deve ser substituida pela a capacidade de cada nó
            //gerada por Nilson
            nodeCapacity.put(node.getHost(), 6);
        }

        int n = 0;
        for (int s = 0; s <= services.size(); s++) {
            int NumUncapacityNodes = 0;
            if (n >= hosts.size()) {
                n = 0;
            }
            boolean serviceAllocated = false;
            while(NumUncapacityNodes < hosts.size() && !serviceAllocated){
                if(nodeCapacity.get(hosts.get(n).getHost()) >= serviceCost.get(services.get(s))){
                    setCellarGroup(services.get(s), hosts.get(n));
                    int newCapacity = nodeCapacity.get(hosts.get(n).getHost()) - serviceCost.get(services.get(s));
                    nodeCapacity.put(hosts.get(n).getHost(), newCapacity);
                    serviceAllocated = true;
                }else{
                    NumUncapacityNodes++;
                }
                n++;
            }
            if (NumUncapacityNodes >= hosts.size()){
                Node greaterNode = greaterCapacity(hosts, nodeCapacity);
                setCellarGroup(services.get(s), greaterNode);
                int newCapacity = nodeCapacity.get(greaterNode.getHost()) - serviceCost.get(services.get(s));
                nodeCapacity.put(hosts.get(n).getHost(), newCapacity);
            }
        }
        //System.out.println("saiu do 'for'.");
        //System.out.println("Fim do blaBli.");
    }
    
    private Node greaterCapacity(ArrayList<Node> nodes, Map<String,Integer> nodeCapacity){
        Node node = nodes.get(0);
        for (int i = 0; i <= nodes.size(); i++) {
            if (nodeCapacity.get(nodes.get(i).getHost()) > nodeCapacity.get(node.getHost()))
                node = nodes.get(i);
        }
        return node;
    }
    
    private void setCellarGroup(String group, Node node) throws Exception{
        Set<Node> ip = new HashSet<Node>();
            ip.add(node);
            ManageGroupCommand command = new ManageGroupCommand(this.cluster.generateId());
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
                                    if (this.cluster.findNodeById(member.getId()) != null) {
                                        buffer.append(member.getId());
                                        if (member.equals(this.cluster.getNode())) {
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
    }

    public void setInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    public void setCluster(ClusterManager cluster) {
        this.cluster = cluster;
    }

    public void setGroup(GroupManager group) {
        this.group = group;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
