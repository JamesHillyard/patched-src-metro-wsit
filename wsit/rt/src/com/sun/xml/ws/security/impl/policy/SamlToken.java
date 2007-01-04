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
 * SamlToken.java
 *
 * Created on February 28, 2006, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
/**
 *
 * @author Abhijit Das,K.Venugopal@sun.com
 */
public class SamlToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.SamlToken, SecurityAssertionValidator{
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private String id;
    private List<String> tokenRefType;
    private String tokenType;
    private PolicyAssertion rdKey = null;
    private String includeTokenType;
    private boolean populated = false;
    private boolean isServer = false;
    /** Creates a new instance of SamlToken */
    
    public SamlToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID id = UUID.randomUUID();
        this.id = id.toString();
    }
    
    public String getTokenType() {
        populate();
        return tokenType;
    }
    
    public Iterator getTokenRefernceType() {
        if ( tokenRefType != null ) {
            return tokenRefType.iterator();
        } else {
            return Collections.emptyList().iterator();
        }
    }
    
    public boolean isRequireDerivedKeys() {
        populate();
        if (rdKey != null ) {
            return true;
        }
        return false;
    }
    
    public String getIncludeToken() {
        populate();
        return includeTokenType;
    }
    
    
    public String getTokenId() {
        return id;
    }
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    
    
    public synchronized void populate() {
        
        if(!populated){
            NestedPolicy policy = this.getNestedPolicy();
            includeTokenType = this.getAttributeValue(itQname);
            if(policy == null){
                if(logger.getLevel() == Level.FINE){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                populated = true;
                return;
            }
            AssertionSet as = policy.getAssertionSet();
            Iterator<PolicyAssertion> paItr = as.iterator();
            
            while(paItr.hasNext()){
                PolicyAssertion assertion  = paItr.next();
                if(PolicyUtil.isSamlTokenType(assertion)){
                    tokenType = assertion.getName().getLocalPart().intern();
                }else if(PolicyUtil.isRequireDerivedKeys(assertion)){
                    rdKey = assertion;
                }else if(PolicyUtil.isRequireKeyIR(assertion)){
                    if(tokenRefType == null){
                        tokenRefType = new ArrayList<String>();
                    }
                    tokenRefType.add(assertion.getName().getLocalPart().intern());
                } else{
                    if(!assertion.isOptional()){
                        if(logger.getLevel() == Level.SEVERE){
                            logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"SamlToken"});
                        }
                        if(isServer){
                            throw new UnsupportedPolicyAssertion("Policy assertion "+
                                    assertion+" is not supported under SamlToken assertion");
                        }
                    }
                }
            }
            populated = true;
        }
    }
    
    
}
