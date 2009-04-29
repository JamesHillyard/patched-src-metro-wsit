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
 * Inicates that the sequence with given sequence identifier is not available.
 * 
 * This exceptions is used under the following conditions:
 *  <ul>
 *      <li>sequence with such {@code sequenceId} is not registered with a given sequence manager</li>
 *  </ul>
 *
 * SOAP Fault data as per WS-RM spec:
 *
 * Properties:
 * [Code] Sender
 * [Subcode] wsrm:UnknownSequence
 * [Reason] The value of wsrm:Identifier is not a known Sequence identifier.
 * [Detail] <wsrm:Identifier ...> xs:anyURI </wsrm:Identifier>
 *
 * Generated by: RM Source or RM Destination.
 * Condition : In response to a message containing an unknown or terminated Sequence identifier.
 * Action Upon Generation : None.
 * Action Upon Receipt : MUST terminate the Sequence if not otherwise terminated.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class UnknownSequenceException extends AbstractSoapFaultException {
    private static final long serialVersionUID = 8837409835889666590L;
    //
    private final String sequenceId;
    
    /**
     * Constructs an instance of <code>NoSuchSequenceException</code> for the sequence with {@code sequenceId} identifier.
     * @param sequenceId the identifier of the unknown sequence.
     */
    public UnknownSequenceException(String sequenceId) {
        super(
                LocalizationMessages.WSRM_1124_NO_SUCH_SEQUENCE_ID_REGISTERED(sequenceId),
                "",
                true);

        this.sequenceId = sequenceId;
    }

    /**
     * Returns the identifier of the unknown sequence
     * @return the unknown sequence identifier
     */
    public String getSequenceId() {
        return sequenceId;
    }        
    @Override
    public Code getCode() {
        return Code.Sender;
    }

    @Override
    public QName getSubcode(RmVersion rv) {
        return rv.unknownSequenceFaultCode;
    }

    @Override
    public void setupDetailElement(Detail detail) {
        // TODO P2 implement
    }

    @Override
    public String getDetailValue() {
        // TODO P2 implement
        return "";
    }
}
