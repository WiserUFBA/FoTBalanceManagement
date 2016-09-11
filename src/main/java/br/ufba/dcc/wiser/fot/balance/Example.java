package br.ufba.dcc.wiser.fot.balance;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Member;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apache.karaf.cellar.hazelcast.HazelcastNode;
import org.apache.karaf.cellar.hazelcast.factory.HazelcastConfigurationManager;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static junit.framework.Assert.assertFalse;
import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.gogo.runtime.threadio.ThreadIOImpl;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.karaf.cellar.core.ClusterManager;
import org.apache.karaf.cellar.core.Group;
import org.apache.karaf.cellar.core.GroupManager;
import org.apache.karaf.cellar.core.Node;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.cellar.core.command.Command;
import org.apache.karaf.cellar.core.command.ExecutionContext;
import org.apache.karaf.cellar.core.control.ManageGroupAction;
import org.apache.karaf.cellar.core.control.ManageGroupCommand;
import org.apache.karaf.cellar.core.control.ManageGroupResult;
import org.apache.karaf.shell.support.table.ShellTable;
import org.junit.Test;

public class Example {

    private HazelcastInstance instance = null;
    private ClusterManager cluster = null;
    private GroupManager group = null;
    private CommandProcessor commandProcessor;
    private ExecutionContext bla;
//    private HazelcastConfigurationManager configuration = null;

    public void setBla(ExecutionContext bla) {
        this.bla = bla;
    }

    public void init() {
        System.out.println("Getting the balance...");
        createFoTgroups();
        commandProcessor = new CommandProcessorImpl(new ThreadIOImpl());
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

//    public void setConfiguration(HazelcastConfigurationManager configuration){
//        this.configuration = configuration;
//    }
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
//        if (!group.isLocalGroup("default")) {
//            group.createGroup("default");
//        }

    }

   
    protected String executeCommand(String command) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        CommandSession commandSession = commandProcessor.createSession(System.in, printStream, System.err);
        //This is required in order to run scripts that use those session variables. 
        //commandSession.put("APPLICATION", System.getProperty("karaf.name", "root"));
        //commandSession.put("USER", "karaf");

        commandSession.execute(command);

        return byteArrayOutputStream.toString();
    }
     
    public void start_bal() throws Exception {

//        @Test
        String listOutput = executeCommand("features:list");
        System.out.println(listOutput);
//        assertFalse(listOutput.isEmpty());
//        listOutput = executeCommand("features:list -i");
//        System.out.println(listOutput);
//        assertFalse(listOutput.isEmpty());
    
        
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
                //group.unRegisterGroup("default");

//                Set<Group> groups = group.listAllGroups();
//                Set<Node> nos = null;
//                for (Group grupo : groups) {
//                    nos = grupo.getNodes();
//                    nos.removeAll(nos);
//                    grupo.setNodes(nos);
//                }

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

        } catch (NullPointerException ex) {
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void blaBli() throws Exception{
        
        Set<Group> groups = group.listAllGroups();
                Set<Node> nos = null;
                for (Group grupo : groups) {
                    nos = grupo.getNodes();
                    nos.removeAll(nos);
                    grupo.setNodes(nos);
                }
     
        ManageGroupCommand command = new ManageGroupCommand("bla");
                command.setDestination(nos);
                command.setAction(ManageGroupAction.JOIN);
                command.setGroupName("security");
                //command.setSourceGroup(null);
                bla.execute(command);

                Map<Node, ManageGroupResult> results = bla.execute(command);
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

}
