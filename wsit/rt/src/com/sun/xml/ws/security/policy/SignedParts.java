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

package com.sun.xml.ws.security.policy;

import com.sun.xml.ws.policy.PolicyAssertion;
import java.util.Iterator;
import javax.xml.namespace.QName;



/**
 * This interface identifies targets in the message that need to be integrity protected. The targets if present should be integrity protected.
 * <p>
 *  <B>Syntax:
 * <p>
 *  <pre><xmp>
 *       <sp:SignedParts ... >
 *            <sp:Body />?
 *            <sp:Header Name="xs:NCName"? Namespace="xs:anyURI" ... />*
 *                  ...
 *       </sp:SignedParts>
 * </xmp> </pre>
 *
 * @author K.Venugopal@sun.com
 */


public interface SignedParts extends Target {
 
    /**
     *
     * @return true if the body is to be integrity protected.
     */
    public boolean hasBody();
    
  
    
    /**
     * {@link java.util.Iterator } over list of Headers that identify targets in the SOAP header
     * to be integrity protected.
     * @return {@link java.util.Iterator }
     */
    public Iterator  getHeaders();  
    
}
