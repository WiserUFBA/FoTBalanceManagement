/*
 * The MIT License
 *
 * Copyright 2017 Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.ufba.dcc.wiser.fot.balance.entity;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import org.apache.karaf.cellar.core.Node;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import br.ufba.dcc.wiser.fot.balance.Controller;
import br.ufba.dcc.wiser.fot.balance.exceptions.UnassociatedHostException;
import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;

/**
 *
 * Group of hosts and bundles.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
@PlanningSolution
public class Group{
    /* Name of the group */
    @SerializedName("groupName")
    private String group_name;
    /* List of hosts */
    private Set<Host> host_list;
    /* List of Bundles */
    @SerializedName("bundlesList")
    private Set<Bundles> bundles_list;
    /* Map of Host Bundle association */
    private final Map<Host, Set<Bundles>> host_bundle_associations;
    
    /* Score for OptaPlanner operations */
    private HardSoftScore score;
    
    /**
     * 
     * Create a FoT Balance Group.
     * 
     * @param group_name Name of the given group.
     */
    public Group(String group_name) {
        /* Set group name */
        this.group_name = group_name;
        
        /* Create the host list */
        host_list = new HashSet<>();
        
        /* Create the bundle list */
        bundles_list = new HashSet<>();
        
        /* Create the map of bundle associations */
        host_bundle_associations = new HashMap<>();
    }
    
    /**
     * 
     * Create a FoT Balance group for clone.
     * 
     */
    public Group(){
        this("");
    }
        
    /**
     * 
     * Set the group name.
     * 
     * @param group_name The new group name.
     */
    public void setGroupName(String group_name){
        /* If the groupName is equal actual name, nothing change */
        if(this.group_name.equals(group_name)){
            FoTBalanceUtils.error("This group already have this name!");
            return;
        }
        
        /* Get Controller Instance */
        Controller controller = Controller.getInstance();
        
        /* Check if new group name hasn't been registered yet on controller */
        if(controller.groupExists(group_name)){
            FoTBalanceUtils.error("Group already exists, choose another name!s");
        }
        
        /* Store the old group name */
        String old_group_name = this.group_name;
        
        /* Set the new group name */
        this.group_name = group_name;
        
        /* Register the new group */
        controller.addGroup(group_name);
        
        /* Remove from old group and put it on the newer */
        for(Host host : host_list){
            /* Get node from host */
            Node node = host.getHostInstance();

            /* Remove from old group name */
            controller.removeHostCellarGroup(node, old_group_name);
            
            /* Add to the newer group */
            controller.addHostCellarGroup(node, group_name);
        }
        
        /* Remove group */
        controller.removeCellarGroup(old_group_name);
    }
    
    /**
     * 
     * Add a host to this group.
     * 
     * @param host Host to add.
     */
    public void addHost(Host host){
        /* Add the host to the host list */
        host_list.add(host);
        
        /* Add a reference of this host to the map of bundles */
        host_bundle_associations.put(host, new HashSet<>());
    }
    
    /**
     * 
     * Remove a given host from the group.
     * 
     * @param host Host that will be removed.
     */
    public void removeHost(Host host){
        /* Remove the host from the host list */
        host_list.remove(host);
        
        /* Pick the list of bundles associated with this host and put it on List of bundles to remove */
        Set<Bundles> old_bundles_associated = host_bundle_associations.get(host);
        host_bundle_associations.remove(host);
        
        /* Remove references of this host on each bundle */
        for(Bundles bundle : old_bundles_associated){
            bundle.disassociateHost();
        }
    }
    
    /**
     * 
     * Remove and unregister all hosts from this group.
     * 
     */
    public void removeAllHosts(){
        /* Get the instance of the controller */
        Controller controller = Controller.getInstance();
        
        /* For each host on host list */
        for(Host host : host_list){
            /* Get the node object from host instance */
            Node node = host.getHostInstance();
            
            /* Remove the host */
            removeHost(host);
            
            /* Remove the node from the cellar group name */
            controller.removeHostCellarGroup(node, group_name);
        }
    }
    
    /**
     * 
     * Return cost of the group.
     * 
     * @return cost of the group.
     */
    public int getGroupCost(){
        /* Group cost */
        int total_group_cost = 0;
        
        /* Recurse from list of bundles */
        for(Bundles bundles : bundles_list){
            total_group_cost += bundles.getBundleCost();
        }
        
        /* Return cost of the given group */
        return total_group_cost;
    }
    
    /**
     * 
     * Add a new bundle to a given group
     * 
     * @param bundle new bundle.
     */
    public void addBundle(Bundles bundle){
        bundles_list.add(bundle);
    }
    
    /**
     * 
     * Return the name of the group.
     * 
     * @return Group name.
     */
    public String getGroupName(){
        return group_name;
    }
    
    /**
     * 
     * Check if the map of associations between bundles and host are updated,
     * this method should be called always after the balance.
     * 
     */
    public void checkMapAssociations(){
        
        /* Clean old associations to avoid conflicts */
        for(Host host : host_list){
            host_bundle_associations.get(host).clear();
        }
        
        /* For each bundle check if the host associated with it isn't null and put in the map */
        for(Bundles bundle : bundles_list){
            
            /* Get host associated with the following bundle */
            Host host_associated = bundle.getHostAssociated();
            
            /* If this bundle has a host instance put in the map */       
            if(host_associated != null){
                host_bundle_associations.get(host_associated).add(bundle);
            }
        }
    }
    
    /**
     * Get all install urls associated with a given host.
     * 
     * @param host Host selected for install urls.
     * @return A list of install urls.
     */
    public List<String> getInstallUrls(Host host){
        /* Get a set of bundles associated with this host */
        Set<Bundles> bundles_associated = getBundlesAssociated(host);
        
        /* List of install urls */
        List<String> install_urls = new ArrayList<>();
        
        /* Get intsall url for each bundle associated with this host */
        for(Bundles bundle : bundles_associated){
            try {
                install_urls.add(bundle.getKarafInstallURL());
            } catch (UnassociatedHostException e) {
                FoTBalanceUtils.error("Unassocaited Host Exception, this bundle is not associated anymore with this host");
                FoTBalanceUtils.trace(e);
            }
        }
        
        return install_urls;
    }
        
    /**
     * Get all uninstall urls associated with a given host.
     * 
     * @param host Host selected for uninstall urls.
     * @return A list of uninstall urls.
     */
    public List<String> getUninstallUrls(Host host){
        /* Get a set of bundles associated with this host */
        Set<Bundles> bundles_associated = getBundlesAssociated(host);
        
        /* List of uninstall urls */
        List<String> uninstall_urls = new ArrayList<>();
        
        /* Get unintsall url for each bundle associated with this host */
        for(Bundles bundle : bundles_associated){
            try {
                uninstall_urls.add(bundle.getKarafInstallURL());
            } catch (UnassociatedHostException e) {
                FoTBalanceUtils.error("Unassocaited Host Exception, this bundle is not associated anymore with this host");
                FoTBalanceUtils.trace(e);
            }
        }
        
        return uninstall_urls;
    }
    
    /**
     * 
     * Get bundles of this group associated with a given host.
     * 
     * @param host Host which bundles are needed.
     * @return A set of bundles associated with a given host.
     */
    public Set<Bundles> getBundlesAssociated(Host host){
        return host_bundle_associations.get(host);
    }
    
    /**
     * 
     * Get Bundle List.
     * 
     * @return Bundle list of this group.
     */
    @PlanningEntityCollectionProperty
    public Set<Bundles> getBundleList(){
        return bundles_list;
    }
    
    public void setBundleList(Set<Bundles> bundles_list) {
        this.bundles_list = bundles_list;
    }
    
    /**
     * 
     * Get Host List.
     * 
     * @return Bundle list of this group.
     */
    @ValueRangeProvider(id = "hostRange")
    @ProblemFactCollectionProperty
    public Set<Host> getHostList(){
        return host_list;
    }
    
    /**
     * 
     * Set Host List.
     * 
     * @param host_list 
     */
    public void setHostList(Set<Host> host_list) {
        this.host_list = host_list;
    }
   
    /**
     * 
     * HardSoft Score OptaPlanner method for retrieve score.
     * 
     * @return Actual score of this operation
     */
    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    /**
     * 
     * Change score of this Group.
     * 
     * @param score 
     */
    public void setScore(HardSoftScore score) {
        this.score = score;
    }
    
    /**
     * 
     * Display association of the bundles of this group with hosts associated with this group.
     * 
     */
    public void displayAssociations() {
        FoTBalanceUtils.info("Group Name -- " + group_name);
        
        for(Host host : host_list){
            FoTBalanceUtils.info("Associated Bundles => " + host.getHostID());
            for(Bundles bundle : bundles_list){
                if(bundle.getHostAssociated() != null && bundle.getHostAssociated().equals(host)){
                    FoTBalanceUtils.info("\t" + bundle.getBundleMvnArtifact());
                }
            }
        }
        FoTBalanceUtils.info("Unassociated Bundles:");
        for(Bundles bundle : bundles_list){
            if(bundle.getHostAssociated() == null){
                FoTBalanceUtils.info("\t" + bundle.getBundleMvnArtifact());
            }
        }
    }
    
    
}
