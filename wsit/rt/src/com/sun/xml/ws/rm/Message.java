/*
 * $Id: Message.java,v 1.1 2006-05-03 22:56:35 arungupta Exp $
 */

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

package com.sun.xml.ws.rm;
import com.sun.xml.ws.rm.protocol.AckRequestedElement;
import com.sun.xml.ws.rm.protocol.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.protocol.SequenceElement;

/**
 * Message is an abstraction of messages that can be added to WS-RM Sequences. 
 * Each instance wraps a JAX-WS message.
 */
public class Message {
    
    /**
     * The JAX-WS Message wrapped by this instance.
     */
    protected com.sun.xml.ws.api.message.Message message;
    
    /**
     * The Sequence to which the message belongs.
     */
    protected Sequence sequence = null;
    
    /**
     * The messageNumber of the Message in its Sequence.
     */
    protected int messageNumber = 0;
    
    
    /**
     * Flag which is true if and only if the message is waiting for
     * a notification.
     */ 
    protected boolean isWaiting = false;
    
    /**
     * Flag indicating whether message is delivered/acked.
     * The meaning differs according to the type of sequence
     * to which the message belongs.  The value must only be
     * changed using the complete() method, which should only
     * be invoked by the Sequence containing the message.
     */
    protected boolean isComplete = false;
    
    
    /**
     * For messages belonging to 2-way MEPS, the corresponding message.
     */
    protected com.sun.xml.ws.rm.Message relatedMessage = null;
    
    /**
     * Sequence stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    protected SequenceElement sequenceElement = null; 
    
    
    /**
     * SequenceElement stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    protected SequenceAcknowledgementElement sequenceAcknowledgementElement = null; 
    
    /**
     * SequenceElement stored when the corresponding com.sun.xml.ws.api.message.Header
     * is added to the message.
     */
    protected AckRequestedElement ackRequestedElement = null; 
    
    /**
     * When true, indicates that the message is a request message for
     * a two-way operation.  ClientOutboundSequence with anonymous
     * AcksTo has to handle Acknowledgements differently in this case.
     */
    public boolean isTwoWayRequest = false;
    
    
    /**
     * Namespace URI corresponding to RM version.
     */
    public static final String namespaceURI = 
            RMBuilder.getConstants().getNamespaceURI();
    
    
    /**
     * Public ctor takes wrapped JAX-WS message as its argument.
     */
    public Message(com.sun.xml.ws.api.message.Message message) {
        this.message = message;
    }
    
    /**
     * Sets  the value of the sequence field.  Used by Sequence methods when
     * adding message to the sequence.
     * @param sequence The sequence.
     */
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
    
    /**
     * Gets the Sequence to which the Message belongs.
     * @return The sequence.
     */
    public Sequence getSequence() {
        return sequence;
    }
    
     /**
     * Sets  the value of the messageNumber field.  Used by Sequence methods when
     * adding message to the sequence.
     * @param messageNumber The message number.
     */
    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }
    
    /**
     * Returns the value of the messageNumber field
     * @return The message number.
     */
    public int getMessageNumber() {
        return messageNumber;
    }
    
    /**
     * Accessor for the relatedMessage field.
     *
     * @return The response corresponding to a request and vice-versa.
     */
    public com.sun.xml.ws.rm.Message getRelatedMessage() {
        return relatedMessage;
    }
    
    /**
     * Mutator for the relatedMessage field.
     *
     * @param mess
     */
    public void setRelatedMessage(com.sun.xml.ws.rm.Message mess) {
        //store the message with a copy of the "inner" com.sun.xml.ws.api.message.Message
        //since the original one will be consumed
        mess.copyContents();
        relatedMessage = mess;
    }
    
    /**
     * Get the RM Header Element with the specified name from the underlying
     * JAX-WS message's HeaderList
     * @param name The name of the Header to find.
     */
    public com.sun.xml.ws.api.message.Header getHeader(String name) {
        if (message == null || !message.hasHeaders()) {
            return null;
        }
        
        return message.getHeaders().get(namespaceURI, name, true);     
    }
    
    /**
     * Add the specified RM Header element to the underlying JAX-WS message's
     * <code>HeaderList</code>.
     *
     * @param header The <code>Header</code> to add to the <code>HeaderList</code>.
     */
    public void addHeader(com.sun.xml.ws.api.message.Header header) {
        message.getHeaders().add(header);
    }
    
    /**
     * Determines whether this message is delivered/acked
     *
     * @return The value of the isComplete flag
     */
    public boolean isComplete() {
        //synchronized block is redundant.
        synchronized(sequence) {
            return isComplete;
        }
    }
    
    /**
     * Sets the isComplete field to true, indicating that the message has been acked. Also
     * discards the stored com.sun.xml.api.message.Message.
     */
    public void complete() {
        //release reference to JAX-WS message.
        synchronized(sequence) {
            message = null;
            isComplete = true;
        }
    }
    
    /**
     * Block the current thread using the monitor of this <code>Message</code>.
     */
     public synchronized void block() {
     
        isWaiting = true;
        try {
            while (isWaiting) {
                wait();
            }
        } catch (InterruptedException e) {}
    }
    
    /**
     * Wake up the current thread which is waiting on this Message's monitor.
     */
    public synchronized  void resume() {
        isWaiting = false;
        notify();
    }
    
    public synchronized boolean isWaiting() {
        return isWaiting;
    }
    
    /**
     * Returns a copy of the wrapped com.sun.xml.ws.api.message.Message.
     */
    public com.sun.xml.ws.api.message.Message getCopy() {
        return message == null ? null : message.copy();
    }
    
    /**
     * Returns a com.sun.ws.rm.Message whose inner com.sun.xml.ws.api.message.Message is replaced by
     * a copy of the original one.  This message is stored in the relatedMessage field of ClientInboundSequence
     * messages.  A copy needs to be retained rather than the original since the original will already
     * have been consumed at such time the relatedMessage needs to be resent.
     *
     */
    public void copyContents() {
        if (message != null) {
            com.sun.xml.ws.api.message.Message newmessage = message.copy();
            message = newmessage;
        }
    }
    
    public String toString() {
        
        String ret = "Message:\n";
        ret += "\tmessageNumber = " + messageNumber + "\n"; 
        Sequence seq = getSequence();
        if (seq != null) {
            ret +="\tsequence = " + getSequence().getId() + "\n";
        } else {
            ret += "\tnone\n";
        }
        
        SequenceElement sel;
        SequenceAcknowledgementElement sael;
        AckRequestedElement ael;
        if ( null != (sel = getSequenceElement())) {
            ret += sel.toString();
        }
        
        if ( null != (sael = getSequenceAcknowledgementElement())) {
            ret += sael.toString();
        }
        
        if ( null != (ael = getAckRequestedElement())) {
            ret += ael.toString();
        }
        
        return ret;
        
        
    }
    
    /*      Diagnostic methods store com.sun.xml.ws.protocol.* elements when
     *      corresponding com.sun.xml.ws.api.message.Headers are added to the 
     *      message
     */
    
    public SequenceAcknowledgementElement getSequenceAcknowledgementElement() {
        return sequenceAcknowledgementElement;
    }
    
    public void setSequenceAcknowledgementElement(SequenceAcknowledgementElement el) {
        sequenceAcknowledgementElement = el;
    }
    
    public SequenceElement getSequenceElement() {
        return sequenceElement;
    }
    
    public void setSequenceElement(SequenceElement el) {
        sequenceElement = el;
    }
    
    public AckRequestedElement getAckRequestedElement() {
        return ackRequestedElement;
    }
    
    public void setAckRequestedElement(AckRequestedElement el) {
        ackRequestedElement = el;
    }
              
}
