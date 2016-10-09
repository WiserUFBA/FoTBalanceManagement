/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.balance;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
        blaBli();
        System.out.println("Todos metodos do 'init' foram executados!");
        
        //createPopulateTables();

    }

    private void createPopulateTables() {
        /* try {
            this.dbConnection = this.dataSource.getConnection();
            Statement stmt = this.dbConnection.createStatement();
            //stmt.execute("drop table sensors_data");
            DatabaseMetaData dbMeta = this.dbConnection.getMetaData();
            System.out.println("Using datasource "
                    + dbMeta.getDatabaseProductName() + ", URL "
                    + dbMeta.getURL());
            stmt.execute("CREATE TABLE IF NOT EXISTS gateways(ID BIGINT AUTO_INCREMENT PRIMARY KEY, ip VARCHAR(255),"
                    + " host VARCHAR(255), tech_comm VARCHAR(255), capacity INT, status INT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS services(ID BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255),"
                    + "tech_comm VARCHAR(255), weight INT)");

//            
//			ResultSet rs = stmt.executeQuery("select * from sensors_data");
//            ResultSetMetaData meta = rs.getMetaData();
//            while (rs.next()) {
//                writeResult(rs, meta.getColumnCount());
//            }
//            rs = stmt.executeQuery("CALL DISK_SPACE_USED('sensors_data')");
//            meta = rs.getMetaData();
//            while (rs.next()) {
//                writeResult(rs, meta.getColumnCount());
//            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
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
    
    private Set<Node> listNode(){
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

        /*//ArrayList<Gateway> gatewayList = new ArrayList<Gateway>();
        String[] nome = {"discovery", "composition", "security", "storage", "localization", "management"}; //array para preenchimento dos serviços
        int[] num = {1, 3, 1, 2, 1, 3};
        // Get all members of the Hazelcast Cluster and display some properties
        
        Set<Services> servicesList = new HashSet<Services>();
        for(int i = 0; i < nome.length; i++){            
            Services s = new Services();            
            s.setName(nome[i]);
            s.setWeigh(num[i]);
            System.out.println(s.getName());
            System.out.println(s.getWeigh());
            servicesList.add(s);              
        }
        */

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

            System.out.println("Balancing made!!!\n");
            
            Set<Node> nos = new HashSet<Node>();
            for (Member member : members) {
                HazelcastNode node = new HazelcastNode(member);
                nos.add(node);
            }

        } catch (NullPointerException ex) {
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void blaBli() throws Exception {

        System.out.println("BBMP!");
        Cluster c = instance.getCluster();
        Set<Node> nodes = new HashSet<Node>();
        Set<Member> members = c.getMembers();
        
        for (Member member : members) {
            HazelcastNode node = new HazelcastNode(member);
            nodes.add(node);
        }
        
        ArrayList<String> services = new ArrayList<String>();
        //String[] nome = {"discovery", "composition", "security", "storage", "localization", "management"};
        services.add("discovery");
        services.add("composition");
        services.add("security");
        services.add("storage");
        services.add("localization");
        services.add("management");
        
        for (int s = 0; s >= services.size(); s++){
            int n = 0;
            if (n <= nodes.size()){
                ManageGroupCommand command = new ManageGroupCommand(this.cluster.generateId());
                command.setDestination(nodes.get(n));
                command.setAction(ManageGroupAction.JOIN);
                command.setGroupName(services.get(s));
                //command.setGroupName(group);
                //command.setSourceGroup(null);
            }else{
                n = 0;

        
        /* Map<Node, ManageGroupResult> results = executionContext.execute(command);
        if (results == null || results.isEmpty()) {
            System.out.println("No result received within given timeout");
        } else {
            ShellTable table = new ShellTable();
            table.column(" ");
            table.column("Group");
            table.column("Members");
            
            for (Node node : results.keySet()) {
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
        }*/
      }  
       
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