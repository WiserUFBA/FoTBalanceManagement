package br.ufba.dcc.wiser.fot.balanceold;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public static BundleContext bc;

    public void start(BundleContext bc) throws Exception {
        System.out.println("Starting the bundle FoT Balance");
        Activator.bc = bc;
    }

    public void stop(BundleContext bc) throws Exception {
        System.out.println("Stopping the bundle FoT Balance");
        Activator.bc = null;
    }

}
