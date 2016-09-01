package br.ufba.dcc.wiser.fot.balance;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Member;
import org.apache.karaf.cellar.hazelcast.HazelcastNode;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.apache.karaf.cellar.core.ClusterManager;
import org.apache.karaf.cellar.core.Group;
import org.apache.karaf.cellar.core.GroupManager;
import org.apache.karaf.cellar.core.Node;

public class Example {

    private HazelcastInstance instance = null;
    private ClusterManager cluster = null;
    private GroupManager group = null;

    public void init() {
        System.out.println("Getting the balance...");
        createFoTgroups();
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

    public void start_bal() {

//        ArrayList<String> info = new ArrayList<String>();

        // Get Cluster with the hazelcast instance
        Cluster cluster = instance.getCluster();

//        ArrayList<Gateway> gatewayList = new ArrayList<Gateway>();
//        String[] nome = {"discovery", "composition", "security", "storage", "localization", "management"}; //array para preenchimento dos serviços
//        int[] num = {1, 3, 1, 2, 1, 3};

        // Get all members of the Hazelcast Cluster and display some properties
        try {
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {
                for (Member member : members) {
                    HazelcastNode node = new HazelcastNode(member);
                    
//                    group.unRegisterGroup("discovery");
//                    group.unRegisterGroup("composition");
//                    group.unRegisterGroup("security");
//                    group.unRegisterGroup("storage");
//                    group.unRegisterGroup("localization");
//                    group.unRegisterGroup("management");
//                    group.registerGroup("default");
                    
                    Group x = group.findGroupByName("composition");
                    Collection<String> z =  Arrays.asList(node.getId());
                    x.setNodes(this.cluster.listNodes(z));
                    group.registerGroup(x);

                    
//                    Gateway gateway = new Gateway(); //os loads não são preenchidos nesse momento pois irão depender dos pesos de cada serviço
//                    gateway.setNode(node.getHost());
//                    gatewayList.add(gateway);

                }
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
//                        i++;
//                    }
//                }
                //info.add(node.getHost());
                //System.out.println(Arrays.toString(info.toArray()));
                //System.out.println("OBJ = " + node.toString());

                //Gateway gateway = new Gateway(); //os loads não são preenchidos nesse momento pois irão depender dos pesos de cada serviço
                //info.setNode("192.168.0." + i);
                //gatewayList.add(gateway);
            }

//            for (Gateway g : gatewayList) { //serve para imprimir os gateways e seus serviços
//                System.out.println(">>>>>>>>>> IpGateway:" + g.getNode() + " >>> Load: " + g.getLoad());
//                for (Services s : g.getServices()) {
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

}
