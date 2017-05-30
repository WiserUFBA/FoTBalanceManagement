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

import br.ufba.dcc.wiser.fot.balance.Bundles;
import br.ufba.dcc.wiser.fot.balance.exceptions.UnassociatedHostException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class BundleTest extends TestSupport {
    
    @Test
    public void testBundleGetMavenUrl(){
        Bundles test_bundle = TestSupport.initBundleObject();
        assertEquals("[Testing] Checking maven test url", TestSupport.GET_MAVEN_URL_TEST_RESULT, test_bundle.getMavenURL());
    }
    
    @Test
    public void testBundleGetKarafUrl(){
        Bundles test_bundle = TestSupport.initBundleObject();
        String karaf_url_test;
        
        try{
            karaf_url_test = test_bundle.getKarafInstallURL();
        } catch(UnassociatedHostException e){
            System.err.println("[Error] No host associated with this test bundle");
            karaf_url_test = "xx";
        }
        
        assertEquals("[Testing] Checking Karaf install url", TestSupport.GET_KARAF_URL_TEST_RESULT, karaf_url_test);
    }
    
    @Test
    public void testBundleGetKarafUrl_Exception(){
        Bundles test_bundle = TestSupport.initBundleObject();
        test_bundle.disassociateHost();
        
        boolean test_result = false;
        try{
            test_bundle.getKarafInstallURL();
        } catch(UnassociatedHostException e){
            test_result = true;
        }
        
        assertEquals("[Testing] Checking if karaf install url throw exception if there's no host", true, test_result);
    }
    
}
