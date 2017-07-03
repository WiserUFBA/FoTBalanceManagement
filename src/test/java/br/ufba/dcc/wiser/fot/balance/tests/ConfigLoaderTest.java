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
package br.ufba.dcc.wiser.fot.balance.tests;

import br.ufba.dcc.wiser.fot.balance.config.GroupConfigFile;
import br.ufba.dcc.wiser.fot.balance.config.HostConfigFile;
import br.ufba.dcc.wiser.fot.balance.config.HostConfigFileObject;
import br.ufba.dcc.wiser.fot.balance.entity.Group;
import com.google.gson.Gson;
import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class ConfigLoaderTest extends TestSupport{
    
    
    /* Return of host config file expected */
    public static String HOST_CONFIG_FILE_SERIALIZED_OUTPUT = "[{\"hostId\":\"Host 1\",\"hostName\":\"host1.local\",\"groupsList\":[\"test1\"]},{\"hostId\":\"Host 2\",\"hostName\":\"host2.local\",\"groupsList\":[\"test2\"]}]";
    
    /* Return of group config file expected */
    public static String GROUP_CONFIG_FILE_SERIALIZED_OUTPUT = "[{\"groupName\":\"test1\",\"bundlesList\":[{\"groupId\":\"GID1\",\"artifactId\":\"AID1\",\"artifactVersion\":\"1.TEST.1\",\"bundleCost\":1.0}]},{\"groupName\":\"test2\",\"bundlesList\":[{\"groupId\":\"GID2\",\"artifactId\":\"AID2\",\"artifactVersion\":\"1.TEST.2\",\"bundleCost\":2.0},{\"groupId\":\"GID3\",\"artifactId\":\"AID3\",\"artifactVersion\":\"1.TEST.3\",\"bundleCost\":3.0}]}]";
    
    @Test
    public void testConfigFileLoader(){
        /* GSON to deserialization of those strings */
        Gson gson = new Gson();
        
        /* Load the two test configuration files */
        List<HostConfigFileObject> host_configurations = HostConfigFile.getConfigurationsFromInstance();
        List<Group> group_configurations = GroupConfigFile.getConfigurationsFromInstance();
        
        /* Now check if the serialization of the list of configurations is the same as our stored string */
        assertEquals("[Testing] Checking if host config file is correct", gson.toJson(host_configurations), HOST_CONFIG_FILE_SERIALIZED_OUTPUT);
        assertEquals("[Testing] Checking if group config file is correct", gson.toJson(group_configurations), GROUP_CONFIG_FILE_SERIALIZED_OUTPUT);
    }
    
}
