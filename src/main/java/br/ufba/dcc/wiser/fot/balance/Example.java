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

public class Example {


    private String a;
    private String b;
    private HazelcastInstance instance = null;
    
    public void setA(String a) {
        this.a = a;
    }

    public void setB(String b) {
        this.b = b;
    }
    
    

    public void bla() {
        System.out.println("Started Example: " + a + " " + b);
    }
    
    public void setInstance(HazelcastInstance instance){
        this.instance = instance;
    }

    public void bli(){
        ArrayList<String> info = new ArrayList<String> ();
        
        System.out.println("Listing node(s): ");
        
        // Check if there's a instance of hazelcast
        if(instance == null){
            System.out.println("erro 1");
            return;
        }

        // Get Cluster with the hazelcast instance
        Cluster cluster = instance.getCluster();
        
        // Get all members of the Hazelcast Cluster and display some properties
        try{
            Set<Member> members = cluster.getMembers();
            if (members != null && !members.isEmpty()) {
                for (Member member : members) {
                    HazelcastNode node = new HazelcastNode(member);
                        
                        info.add(node.getHost());
                        System.out.println(Arrays.toString(info.toArray()));
                        //System.out.println("OBJ = " + node.toString());
                  
                }

            }
        }
        catch(NullPointerException ex){
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
}
