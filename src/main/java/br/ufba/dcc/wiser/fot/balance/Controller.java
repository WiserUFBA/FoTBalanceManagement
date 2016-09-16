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

/**
 *
 * @author juran
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
        createPopulateTables();
    }

    private void createPopulateTables() {
        try {
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

            /*
			ResultSet rs = stmt.executeQuery("select * from sensors_data");
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                writeResult(rs, meta.getColumnCount());
            }
            rs = stmt.executeQuery("CALL DISK_SPACE_USED('sensors_data')");
            meta = rs.getMetaData();
            while (rs.next()) {
                writeResult(rs, meta.getColumnCount());
            }*/

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public void verifyNodesChanges() throws Exception {
        boolean change = true;
        if (change) {
            startBalance();
        }
    }

    private void startBalance() throws Exception {

        //contar demanda existente
        // - supondo que entrou um novo gateway
        //     - verificar se ele já faz parte da tabela de gateways
        //             - caso SIM alterar o status e nao acrescentar nada a demanda
        //             - caso seja um novo gateway acrescentar sua demanda prévia
        // - supondo que saiu um gateway
        //      - alterar  status para OFF
        //retira de todos os grupos
        //inicia distribuicao da demanda para todos os gateway com status ON
        
        
//        System.out.println(executeCommand("gosh"));
        ArrayList<String> info = new ArrayList<String>();

        // Get Cluster with the hazelcast instance
        Cluster cluster = instance.getCluster();

//        ArrayList<Gateway> gatewayList = new ArrayList<Gateway>();
//        String[] nome = {"discovery", "composition", "security", "storage", "localization", "management"}; //array para preenchimento dos serviços
//        int[] num = {1, 3, 1, 2, 1, 3};
        // Get all members of the Hazelcast Cluster and display some properties
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
//                  
//                    Group x = group.findGroupByName("composition");
//                    Collection<String> z =  Arrays.asList(node.getId());
//                    x.setNodes(this.cluster.listNodes(z));
//                    group.registerGroup(x);
//                    Gateway gateway = new Gateway(); //os loads não são preenchidos nesse momento pois irão depender dos pesos de cada serviço
//                    gateway.setNode(node.getHost());
//                    gatewayList.add(gateway);
//                    
//
//                }
//                int i = 0; //utilizado apenas para iterar os serviços que serão preenchidos dentro de cada gateway
//                for (Gateway g : gatewayList) {
//                    for (int j = 0; j < 2; j++) {
//                        Services service = new Services();
//                        if (i >= 3) { //apenas para volta o contador de serviço
//                            i = 0;
//                        }
//                        service.setName(nome[i]);
//                        service.setWeigh(num[i]);
//                        g.setLoad(g.getLoad() + num[i]); //preste atenção nessa linha - o valor de Load está sendo somado com o valor do peso do serviço
//                        g.getServices().add(service);
//                        
//                        i++;
//                    }
                }
                //info.add(node.getHost());
                //System.out.println(Arrays.toString(info.toArray()));
                //System.out.println("OBJ = " + node.toString());

                //Gateway gateway = new Gateway(); //os loads não são preenchidos nesse momento pois irão depender dos pesos de cada serviço
                //info.setNode("192.168.0." + i);
                //gatewayList.add(gateway);
            }

//            for (Gateway g : gatewayList) { //serve para imprimir os gateways e seus serviços
//                System.out.println("\n>>>>>>>>>> IpGateway:" + g.getNode() + " >>> Load: " + g.getLoad());
//                
//                for (Services s : g.getServices()) {
//                    
////                    group.registerGroup(s.getName());
//                    System.out.println(">>>>>>>>>> ServiceName:" + s.getName() + " >>> Weigh:" + s.getWeigh());
//                }
//                //System.out.println("#########################");
//            }
            System.out.println("Balancing made!!!\n");
            blaBli();

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
        ManageGroupCommand command = new ManageGroupCommand(this.cluster.generateId());
        command.setDestination(nodes);
        command.setAction(ManageGroupAction.JOIN);
        command.setGroupName("security");
        //command.setSourceGroup(null);
        Map<Node, ManageGroupResult> results = executionContext.execute(command);
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
