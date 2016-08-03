package br.ufba.dcc.wiser.fot.balance;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Member;
//import java.util.ArrayList;
//import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.karaf.cellar.core.discovery.DiscoveryService;
//import org.apache.karaf.cellar.core.utils.CombinedClassLoader;
import org.apache.karaf.cellar.hazelcast.HazelcastNode;
import org.apache.karaf.cellar.hazelcast.SingletonStaticFactory;
import org.apache.karaf.cellar.hazelcast.factory.HazelcastConfigurationManager;
import org.apache.karaf.cellar.hazelcast.factory.HazelcastServiceFactory;
//import org.osgi.framework.ServiceReference;
//import org.osgi.util.tracker.ServiceTracker;
//import org.osgi.util.tracker.ServiceTrackerCustomizer;


public class Example {

    private String a;
    private String b;  
    
    public void setA(String a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }

    public void bla() {
        System.out.println("Started Example: " + a + " " + b);
    }

    public void bli(){

        System.out.println("Listing node: ");

        if(SingletonStaticFactory.getInstance() == null){
            System.out.println("erro 1");
            return;
        }
        
        HazelcastServiceFactory factory = SingletonStaticFactory.getInstance();
        HazelcastInstance factoryInstance = null;
        try {
            factoryInstance = factory.getInstance();
        } catch (InterruptedException ex) {
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        Cluster cluster = factoryInstance.getCluster();
        cluster.getMembers();
        
        if (cluster != null) {
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {
                for (Member member : members) {
                    HazelcastNode node = new HazelcastNode(member);
                    System.out.println( //"ID = " + node.getId() +
                                        //", NODE = " + node.getHost() +
                                        //", ALIAS = " + node.getAlias() +
                                        ", OBJ = " + node.toString());
                }
            }
        }
        else{
            System.out.println("erro 3");
        }
    
    }
}
