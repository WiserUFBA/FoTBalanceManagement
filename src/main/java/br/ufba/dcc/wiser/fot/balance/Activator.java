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
package br.ufba.dcc.wiser.fot.balance;

import br.ufba.dcc.wiser.fot.balance.utils.FoTBalanceUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * FoT Balance Activator.
 *
 * @author Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>
 */
public class Activator implements BundleActivator {

    public static BundleContext bc;

    @Override
    public void start(BundleContext bc) throws Exception {
        FoTBalanceUtils.info("Starting the bundle FoT Balance Management");
        Activator.bc = bc;
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        FoTBalanceUtils.info("Stopping the bundle FoT Balance Management");
        
        /* Get a controller instance and stop all functions */
        FoTBalanceUtils.debug("Getting controller instance");
        Controller controller = Controller.getInstance();
        
        /* Destroy session */
        //controller.destroySession();
        
        /* Destroy controller instance */
        //Controller.destroyInstance();
        
        /* Clean bundle context to avoid errors */
        Activator.bc = null;
        
        /* End of stop */
        FoTBalanceUtils.debug("Fot Balance Management stopped");
    }

}
