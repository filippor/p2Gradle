package it.filippor.p2.impl.samples;
//package it.filippor.p2.impl;
//
//import org.eclipse.equinox.internal.p2.garbagecollector.GarbageCollector;
//import org.eclipse.equinox.p2.core.IProvisioningAgent;
//import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
//import org.eclipse.equinox.p2.core.ProvisionException;
//import org.eclipse.equinox.p2.engine.IProfile;
//import org.eclipse.equinox.p2.engine.IProfileRegistry;
//
//public class P2GarbageCollector {
//  private void test() throws ProvisionException {
//      IProvisioningAgentProvider provider = null;// obtain the IProvisioningAgentProvider using OSGi services
//        IProvisioningAgent agent = provider.createAgent(null);  // null = location for running system
//        if(agent == null) throw new RuntimeException("Location was not provisioned by p2");
//        IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
//        if (profileRegistry == null) throw new RuntimeException("Unable to acquire the profile registry service.");
//        // can also use IProfileRegistry.SELF for the current profile
//        IProfile profile = profileRegistry.getProfile("SDKProfile");
//        GarbageCollector gc = (GarbageCollector) agent.getService(GarbageCollector.SERVICE_NAME);
//        gc.runGC(profile);
//        
//  }
//}
