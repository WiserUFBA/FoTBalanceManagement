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
package br.ufba.dcc.wiser.fot.balance.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Configuration file content class.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class HostConfigFileObject {
    
    /* Id of this host */
    private String hostId;
    
    /* IP of this host */
    private String hostName;
    
    /* List of groups */
    private List<String> groupsList;
    
    /**
     * 
     * Instantiate a new Configuration File.
     * 
     * @param hostId Id of this host.
     * @param hostName FQDN of this host.
     * @param groupsList Array of groups which is associated with this host.
     */
    public HostConfigFileObject(String hostId, String hostName, String[] groupsList){
        this.hostId = hostId;
        this.hostName = hostName;
        this.groupsList = new ArrayList<>(Arrays.asList(groupsList));
    }
    
    /**
     * 
     * Instantiate a new configuration file without groups.
     * 
     * @param hostId Id of this host.
     * @param hostName FQDN of this host.
     */
    public HostConfigFileObject(String hostId, String hostName){
        this(hostId, hostName, new String[0]);
    }
    
    /**
     * 
     * Instantiate a new configuration file without groups and IP.
     * 
     * @param hostId Id of this host.
     */
    public HostConfigFileObject(String hostId){
        this(hostId, "");
    }
    
    /**
     * 
     * No argument constructor for clone operations.
     * 
     */
    public HostConfigFileObject(){
        this("");
    }

    /**
     * 
     * Get list of groups
     * 
     * @return a list of groups id.
     */
    public List<String> getGroupsList() {
        return groupsList;
    }

    /**
     * 
     * Set a list of groups in this object.
     * 
     * @param groupsList New list of groups id.
     */
    public void setGroupsList(List<String> groupsList) {
        this.groupsList = groupsList;
    }    
    
    /**
     * 
     * Get id of this host.
     * 
     * @return Id of this host.
     */
    public String getHostId() {
        return hostId;
    }

    /**
     * 
     * Set id of this host.
     * 
     * @param hostId New host id.
     */
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
    
    /**
     * 
     * Get IP of this host.
     * 
     * @return FQDN of this host.
     */
    public String getHostIp() {
        return hostName;
    }

    /**
     * 
     * Set id of this host.
     * 
     * @param hostName New FQDN of this host.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
