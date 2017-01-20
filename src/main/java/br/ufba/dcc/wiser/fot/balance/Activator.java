package br.ufba.dcc.wiser.fot.balance;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public static BundleContext bc;

    @Override
    public void start(BundleContext bc) throws Exception {
        System.out.println("Starting the bundle FoT Balance");
        Activator.bc = bc;
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        System.out.println("Stopping the bundle FoT Balance");
        Activator.bc = null;
    }

}
