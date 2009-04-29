/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;

/**
 * If the condition listed below is reached, the RM Destination MUST generate this fault.
 *
 * Properties:
 * [Code] Sender
 * [Subcode] wsrm:MessageNumberRollover
 * [Reason] The maximum value for wsrm:MessageNumber has been exceeded.
 * [Detail] <wsrm:Identifier ...> xs:anyURI </wsrm:Identifier>, <wsrm:MaxMessageNumber> wsrm:MessageNumberType </wsrm:MaxMessageNumber>
 *
 * Generated by: RM Source or RM Destination.
 * Condition : Message number in /wsrm:Sequence/wsrm:MessageNumber of a Received message exceeds the internal limitations of an RM Destination or reaches the maximum value of 9,223,372,036,854,775,807.
 * Action Upon Generation : RM Destination SHOULD continue to accept undelivered messages until the Sequence is closed or terminated.
 * Action Upon Receipt : RM Source SHOULD continue to retransmit undelivered messages until the Sequence is closed or terminated.
 *
 * @author m_potociar
 */
public final class MessageNumberRolloverException extends AbstractSoapFaultException {
    private static final long serialVersionUID = 7692916640741305184L;
    //
    private long messageNumber;
    private String sequenceId;

    public long getMessageNumber() {
        return messageNumber;
    }

    public String getSequenceId() {
        return sequenceId;
    }    
    
    public MessageNumberRolloverException(String sequenceId, long messageNumber) {
        // TODO L10N localization code change
        super(
                LocalizationMessages.WSRM_1138_MESSAGE_NUMBER_ROLLOVER(sequenceId, messageNumber),
                "The maximum value for wsrm:MessageNumber has been exceeded.",
                true);
        
        this.messageNumber = messageNumber;
        this.sequenceId = sequenceId;
    }

    @Override
    public Code getCode() {
        return Code.Sender;
    }

    @Override
    public QName getSubcode(RmVersion rv) {
        return rv.messageNumberRolloverFaultCode;
    }

    @Override
    public String getDetailValue() {
        return ""; // TODO P2 implement
    }

    @Override
    public void setupDetailElement(Detail detail) {
        // TODO P2 implement
    }
}
