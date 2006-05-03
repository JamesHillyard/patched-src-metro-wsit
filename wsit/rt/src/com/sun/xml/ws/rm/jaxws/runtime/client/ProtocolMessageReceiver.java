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
 * ProtocolMessageReceiver.java
 *
 * @author Mike Grogan
 * Created on March 1, 2006, 12:50 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.protocol.CreateSequenceResponseElement;
import com.sun.xml.ws.rm.protocol.SequenceAcknowledgementElement;
import com.sun.xml.ws.transport.http.server.EndpointImpl;

import javax.xml.ws.soap.SOAPBinding;
import java.util.HashMap;


/**
 * Handles the contents of responses to RM Protocol requests with
 * non-anonymous AcksTo.
 */
public class ProtocolMessageReceiver {

     public static final String anonymous = new RMConstants()
            .getAddressingBuilder().newAddressingConstants().getAnonymousURI();
    /**
     * AcksTo URI used for non-anonymous responses... Currently one per process.
     * Set using the <code>start</code> method.  Defaults to anonymous.  When
     * start is called with non-anonymous argument, an HTTP listener is started to
     * process the messages.
     */
    private static String acksTo = anonymous;
    
    /**
     * Endpoint listening for protocol messages.
     */
    private static EndpointImpl endpoint;
    
    /**
     * Map of  messageId String / CreateSequenceElement pairs that have beeen
     * passed to setCreateSequenceResponse.
     */
    private static HashMap<String, CreateSequenceResponseElement> knownIds = 
            new HashMap<String, CreateSequenceResponseElement>();
    
    
    
    /**
     * Everything is static
     */
    private ProtocolMessageReceiver() {
    }
    
    /**
     * Accessor for the AcksTo field.
     */
    public static String getAcksTo() {
        return acksTo;
    }
    
    /*
     * Set the acksTo field to the specified URI and start the
     * Http listener listening at that URI.
     */
    public static void start(String newAcksTo) {
        
        if (!acksTo.equals(anonymous) && !newAcksTo.equals(acksTo)) {
            throw new UnsupportedOperationException("Cannot change non-anonymous acksTo");
        }
        
        if (acksTo.equals(anonymous)) {
            acksTo = newAcksTo;      
            //start our endpoint listening on the given URI
            BindingID binding = BindingID.parse(SOAPBinding.SOAP12HTTP_BINDING) ;
            endpoint = new EndpointImpl(binding, new DummyProvider());
            endpoint.publish(acksTo);

        }
    }
    
    public static void stop() {
        if (endpoint != null) {
            endpoint.stop();
        }
    }
    
       
    public static void setCreateSequenceResponse(String messageId, 
            CreateSequenceResponseElement csrElement) {
        
        synchronized (knownIds) {
            knownIds.put(messageId, csrElement);
            knownIds.notifyAll();
        }
    }
    
    public static CreateSequenceResponseElement 
            getCreateSequenceResponse(String messageId) {
        
        CreateSequenceResponseElement ret = null;
        synchronized (knownIds) {
            if (!knownIds.keySet().contains(messageId)) {
                knownIds.put(messageId, null);
            }
           
            
            while (null == (ret =  knownIds.get(messageId))) {
                try {
                    knownIds.wait();
                } catch (InterruptedException e){}
            }
        }
        return ret;
    }
                
      
    
    public static void handleAcknowledgement(SequenceAcknowledgementElement el)
                        throws RMException {
       //probably no need for synchronization here.  The element was initialized at the 
       //endpoint using a sequenceid generated by the endpoint that made it back to
       //the client.  That means that getCreateSequenceResponse has returned long ago.
        String id = el.getId();
        ClientOutboundSequence seq = RMSource.getRMSource().getOutboundSequence(id);
        if (id == null) {
            throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,id),id);
        }
        
        seq.handleAckResponse(el);
        
    }
    
}
