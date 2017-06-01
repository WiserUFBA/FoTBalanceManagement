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

import br.ufba.dcc.wiser.fot.balance.entity.Group;
import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import com.google.gson.Gson;
import java.util.List;


/**
 *
 * Load of the group configuration file.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class GroupConfigFile {
    
    /* URL to this configuration file */
    private static final String GROUP_CONFIGURATION_FILE_URL = "/br/ufba/dcc/wiser/fot/balance/config/group_config.json";
    
    /* Configurations list */
    private final List<Group> configurations;
    
    /* Instance of this object */
    private static GroupConfigFile instance = null;
    
    /* Private constructor for singlenton operations */
    private GroupConfigFile(){
        configurations = ConfigLoader.configLoader(GROUP_CONFIGURATION_FILE_URL);
        Gson gson = new Gson();
        FoTBalanceUtils.infoMsg("Configuration file loaded!");
        FoTBalanceUtils.infoMsg("Content: " + gson.toJson(configurations));
    }
    
    /**
     * Get Configurations from host configuration file instance 
     * 
     * @return An instance of Host Configuration file properties.
     */
    public static GroupConfigFile getInstance(){
        if(instance == null){
            instance = new GroupConfigFile();
        }
        
        return instance;
    }
    
    /**
     * 
     * Return a list of configuration objects.
     * 
     * @return A list of configurations object.
     */
    public List<Group> getConfigurations(){
        return configurations;
    }
    
    /**
     * 
     * Return configurations list from configuration instance.
     * 
     * @return A list of configuration objects.
     */
    public static List<Group> getConfigurationsFromInstance(){
        return instance.getConfigurations();
    }
    
}
