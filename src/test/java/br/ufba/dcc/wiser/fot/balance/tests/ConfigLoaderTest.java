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

import br.ufba.dcc.wiser.fot.balance.config.HostConfigFileObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class ConfigLoaderTest extends TestSupport{
    
    /* Basic JSON string for bundle test purpose */
    public static final String DEFAULT_BUNDLE_CONFIG_TEST = 
        "[\n" +
        "    {\n" +
        "        \"group_name\": \"test_group\",\n" +
        "        \"bundles_list\": [\n" +
        "            {\n" +
        "                \"groupId\" : \"groupId\",\n" +
        "                \"artifactId\" : \"artifactId\",\n" +
        "                \"artifactVersion\" : \"1.0.0\",\n" +
        "                \"bundleCost\" : 1\n" +
        "            }\n" +
        "        ]\n" +
        "    }\n" +
        "]";
    
    /* Basic JSON string for host test purpose */
    public static final String DEFAULT_HOST_CONFIG_TEST = 
        "[\n" +
        "    {\n" +
        "        \"hostId\": \"test_host\",\n" +
        "        \"groupsList\": [\n" +
        "            \"security\",\n" +
        "            \"basic\"\n" +
        "        ]\n" +
        "    }\n" +
        "]";
    
    @Test
    public void testConfigFile(){
        // TODO
        Gson gson = new Gson();
        
        Type listOfHostsConfigFileObjectType = new TypeToken<ArrayList<HostConfigFileObject>>(){}.getType();
        
        List<HostConfigFileObject> hostsConfiguration = gson.fromJson(DEFAULT_HOST_CONFIG_TEST, listOfHostsConfigFileObjectType);
        
        System.out.println("FINAL OBJECT => " + gson.toJson(hostsConfiguration));
        
        assertEquals("[Testing] Checking if ", 0, 0);
    }
    
}
