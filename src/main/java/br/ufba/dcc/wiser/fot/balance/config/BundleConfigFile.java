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

import br.ufba.dcc.wiser.fot.balance.Group;
import com.google.gson.annotations.SerializedName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class BundleConfigFile {
    
    /* List of groups */
    @SerializedName("FotBalanceGroups")
    private Set<Group> groups_list;
    
    /**
     * 
     * Instantiate a new Configuration File.
     * 
     */
    public BundleConfigFile(){
        groups_list = new HashSet();
    }

    /**
     * 
     * Get list of groups
     * 
     * @return a set of groups.
     */
    public Set<Group> getGroupsList() {
        return groups_list;
    }

    /**
     * 
     * Set a list of groups in this object.
     * 
     * @param groups_list New list of groups.
     */
    public void setGroupsList(Set<Group> groups_list) {
        this.groups_list = groups_list;
    }
    
}
