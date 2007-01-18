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

package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import static com.sun.xml.ws.tx.common.Constants.*;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * @author jf39279
 */
public class TxPolicyValidator implements PolicyAssertionValidator {

    private static final ArrayList<QName> supportedAssertions = new ArrayList<QName>();

    static {
        // In Future: will  need to support both member submission and OASIS WS-TX WS-AT assertions.
        supportedAssertions.add(AT_ASSERTION);
        supportedAssertions.add(AT_ALWAYS_CAPABILITY);
    }

    public Fitness validateClientSide(final PolicyAssertion assertion) {
        return supportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }

    public Fitness validateServerSide(final PolicyAssertion assertion) {
        return supportedAssertions.contains(assertion.getName()) ? Fitness.SUPPORTED : Fitness.UNKNOWN;
    }

    public String[] declareSupportedDomains() {
        return new String[] {WSAT_SOAP_NSURI};
    }

}
