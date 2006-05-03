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

package com.sun.xml.ws.policy;

import javax.xml.namespace.QName;

/**
 * This class provides implementation of PolicyMapKey interface to be used in connection with {@link WSPolicyMap}.
 * Instances of the class are created by a call to one of {@link WSPolicyFactory} <code>createWsdl<emph>XXX</emph>PolicyMapKey(...)</code>
 * methods.
 * <p/>
 * The class wraps scope information and adds a package setter method to allow injection of actual equality comparator/tester. This injection
 * is made within a <code>get...</code> call on {@link WSPolicyMap}, before the actual scope map search is performed.
 * 
 * 
 * @author Marek Potociar
 */
final public class PolicyMapKey  {
    QName service;
    QName port;
    QName operation;
    QName faultMessage;
//    QName inputMessage;
//    QName outputMessage;
    
    private PolicyMapKeyHandler handler;
    
    PolicyMapKey(QName service, QName port, QName operation) {
        this.service = service;
        this.port = port;
        this.operation = operation;
        
//        this.inputMessage = inputMessage;
//        this.outputMessage = outputMessage;
    }
    
    PolicyMapKey(QName service, QName port, QName operation, QName faultMessage) {
        this.service = service;
        this.port = port;
        this.operation = operation;
        
//        this.inputMessage = inputMessage;
//        this.outputMessage = outputMessage;
        
        this.faultMessage = faultMessage;
    }
    
    PolicyMapKey(PolicyMapKey that) {
        this.service = that.service;
        this.port = that.port;
        this.operation = that.operation;
        this.faultMessage = that.faultMessage;

//        this.inputMessage = that.inputMessage;
//        this.outputMessage = that.outputMessage;

        this.handler = that.handler;
    }
    
    void setHandler(PolicyMapKeyHandler handler) {
        this.handler = handler;
    }
    
    public boolean equals(Object that) {
        if (this == that) {
            return true; // we are lucky here => no special handling is required
        }
        
        if (that == null) {
            return false;
        }
            
        if (handler == null) {
            throw new IllegalStateException("Policy map key handler is not set.");
        }
        
        if (that instanceof PolicyMapKey) {
            return handler.areEqual(this, (PolicyMapKey) that);
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (handler == null) {
            throw new IllegalStateException("Policy map key handler is not set.");
        }

        return handler.generateHashCode(this);
    }    
    
    public String toString() {
        StringBuffer result = new StringBuffer("WsdlPolicyMapKey(");
        result.append(this.service).append(", ").append(port).append(", ").append(operation).append(", ").append(faultMessage);
        return result.append(")").toString();
    }
}
