/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package xwss.s14.server;

                                                                                                                                                             
@javax.jws.WebService (endpointInterface="xwss.s14.server.IPingService")
public class PingImpl implements IPingService {
    
   public String ping(String message) {
        System.out.println("The message is here : " + message);
        return message;
    }                    
}
