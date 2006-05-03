/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/*
 * RMSource.java
 *
 * @author Mike Grogan
 * Created on October 15, 2005, 6:24 PM
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.RMProvider;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;


import javax.xml.ws.addressing.EndpointReference;
/**
 * An RMSource represents a Collection of RMSequences with a 
 * common acksTo endpoint.
 */
public class RMSource  extends RMProvider<ClientInboundSequence,
                                          ClientOutboundSequence> {
   
    private static RMSource rmSource = new RMSource();
    private long retryInterval;
    private static boolean running;
    
    public static RMSource getRMSource() {
        return rmSource;
    }
    
   
    public RMSource() {

        retryInterval = 2000;
        running = true;
        
        RetryThread t = new RetryThread();
        
        //Make sure that retry thread will not prevent
        //application from exiting.
        t.setDaemon(true);
        t.start();
    }
    
    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }
    
    public long getRetryInterval() {
        return retryInterval;
    }
    
    
    public void terminateSequence(ClientOutboundSequence seq) throws RMException {
        
        String id = seq.getId();
        if (seq != null) {
            seq.disconnect();
            outboundMap.remove(id);
        }
    }
   
  
   
    public void addInboundSequence(ClientInboundSequence seq) {
        inboundMap.put(seq.getId(), seq);
    }
    
    
    public void addOutboundSequence(ClientOutboundSequence seq) {
        outboundMap.put(seq.getId(), seq);
    }
     
    /**
     * Allow a clean shutdown
     *
     * @deprecated - This was a plugfest hack.  The Retry thread is now a 
     * daemon, obviating the need to shut it down.  At the moment, we do not
     * need a ProtocolMessageReceiver, since we are not doing duplex
     * bindings.
     */
    public static void stop() {
        
        //allow the RetryThread to exit
        running = false;
        
        //allow the Protocol message listenter, if any to stop listening
        ProtocolMessageReceiver.stop();
    }
    
    private class RetryThread extends Thread {
        
        public void run() {
            try {
                while (running) {
                    Thread.sleep(retryInterval);
              
                    for (String key : outboundMap.keySet()) {
                  
                        ClientOutboundSequence seq =
                                getOutboundSequence(key);
                        
                        synchronized(seq) {
                            //1. resend all incomplete messages
                            //2. send ackRequested messages in any sequences
                            //   in danger of timing out.
                            seq.doMaintenanceTasks();
                        }
                        
                             
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
