/*
 * $Id: Renewing.java,v 1.1 2006-05-03 22:57:18 arungupta Exp $
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

package com.sun.xml.ws.security.trust.elements;

/**
 * Used to specify renew semantics for types that support this operation.
 *
 * @author WS-Trust Implementation Team
 */
public interface Renewing {

    /**
     * Gets the value of the allow property.
     *
     * @return {@link Boolean }
     *     
     */
    Boolean isAllow();

    /**
     * Gets the value of the ok property.
     * 
     * @return {@link Boolean }
     *     
     */
    Boolean isOK();

    /**
     * Sets the value of the allow property.
     * 
     * @param value {@link Boolean }
     *     
     */
    void setAllow(Boolean value);

    /**
     * Sets the value of the ok property.
     * 
     * @param value {@link Boolean }
     *     
     */
    void setOK(Boolean value);
    
}
