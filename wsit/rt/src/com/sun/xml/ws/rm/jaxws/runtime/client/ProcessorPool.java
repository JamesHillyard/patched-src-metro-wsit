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
 * ProcessorPool.java
 *
 * @author Mike Grogan
 * Created on March 23, 2006, 1:08 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import java.util.Stack;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.message.Packet;
/**
 * Pool of Pipelines used by RMClientPipe to insure that
 * no two invocations of nextPipe.process() are done concurrently.
 */
public class ProcessorPool<T extends Pipe> extends Stack<T> {
    
    private T pipe;
    
    /** Creates a new instance of ProcessorPool */
    public ProcessorPool(T pipe){
        this.pipe =pipe;
    }
    
    public synchronized T checkOut() {
        
        if (!isEmpty()) {
            return pop();
        } else {
            return (T)PipeCloner.clone(pipe);
        }
    } 
    
    public synchronized void checkIn(T in) {
        push(in);
    }
    
   
}
