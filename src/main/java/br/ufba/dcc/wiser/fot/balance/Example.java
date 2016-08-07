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
        
        ArrayList<String> ip = new ArrayList();
    
        // [ B ] usando o método add() para gravar 5 números de IP
//        ip.add("192.168.0.1");
//        ip.add("192.168.0.2");
        ip.add("192.168.0.3");
//        ip.add("192.168.0.4");
//        ip.add("192.168.0.5");
        
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
                        //System.out.println(Arrays.toString(info.toArray()));
                        //System.out.println("OBJ = " + node.toString());
                  
                }
                
                System.out.println(">>>>"+ ip.equals(info));

            }
        }
        catch(NullPointerException ex){
            System.out.println("erro 2");
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
}
