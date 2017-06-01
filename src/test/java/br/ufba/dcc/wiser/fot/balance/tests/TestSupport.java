/*
 * The MIT License
 *
 * Copyright 2017 jeferson.
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

import br.ufba.dcc.wiser.fot.balance.entity.Bundles;
import br.ufba.dcc.wiser.fot.balance.entity.Group;
import br.ufba.dcc.wiser.fot.balance.entity.Host;

/**
 *
 * Test support of balance application.
 * 
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public abstract class TestSupport {
    
    /* Test Maven URL return */
    public static final String GET_MAVEN_URL_TEST_RESULT = "mvn=testGroup/testArtifact/1.0.0.TEST";
    
    /* Test Karaf URL return */
    public static final String GET_KARAF_URL_TEST_RESULT = "url:http://localhost:8181/bundleInstall?" + GET_MAVEN_URL_TEST_RESULT;
    
    /* Return a test bundle object */
    public static Bundles initBundleObject(){
        Bundles test_bundle = new Bundles("testArtifact", "testGroup", "1.0.0.TEST", 1);        
        Host test_Host = new Host(1);
        test_bundle.setHostAssociated(test_Host);
        return test_bundle;
    }
    
    /* Return a test group object */
    public static Group initGroupObject(){
        Group test_group = new Group("testGroup");
        return test_group;
    }
}
