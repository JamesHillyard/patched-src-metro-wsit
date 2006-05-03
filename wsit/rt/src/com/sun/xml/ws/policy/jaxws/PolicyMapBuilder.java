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
package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapMutator;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Used for populating changes into PolicyMap. Once a PolicyMap is created
 * PolicyMapBuilder notifies all the registered WSPolicyBuilderHandler to populate
 * changes to the PolicyMap.
 * 
 * 
 * @author japod
 */
class PolicyMapBuilder {
    
    
    /**
     * policyBuilders should contain list of registered PolicyBuilders
     */
    private List<BuilderHandler> policyBuilders;
    

    /**
     * Creates a new instance of PolicyMapBuilder
     */
    PolicyMapBuilder() {
    }
    
    
    /**
     *     Registers another builder, which has to be notified after a new
     *     PolicyMap is created in order to populate it's changes. 
     *     
     */
    void registerHandler(BuilderHandler builder){
        if (null!=builder) {
            getPolicyBuilders().add(builder);
        }
    }

    /**
     * Iterates all the registered PolicyBuilders and lets them populate 
     * their changes into PolicyMap
     */
    PolicyMap getPolicyMap() throws PolicyException{
        return getNewPolicyMap(null);
    }    

    /**
     * Iterates all the registered PolicyBuilders and lets them populate 
     * their changes into PolicyMap. Registers mutator given as a parameter
     * with the newly created map.
     */
    PolicyMap getPolicyMap(PolicyMapMutator mutator) throws PolicyException{
        if (null==mutator) {
            return getNewPolicyMap(null);
        } else {
            LinkedList<PolicyMapMutator> list = new LinkedList<PolicyMapMutator>();
            list.add(mutator);
            return getNewPolicyMap(list);
        }
    }    
    
    /**
     * Iterates all the registered PolicyBuilders and lets them populate 
     * their changes into PolicyMap. Registers mutators from collection given as a parameter
     * with the newly created map.
     */
    PolicyMap getPolicyMap(Collection<PolicyMapMutator> externalMutators) throws PolicyException{
        return getNewPolicyMap(externalMutators);
    }
    
    
    /**
     * Iterates all the registered PolicyBuilders and lets them populate 
     * their changes into PolicyMap. Registers mutators from collection given as a parameter
     * with the newly created map.
     */
    private PolicyMap getNewPolicyMap(Collection<PolicyMapMutator> externalMutators) throws PolicyException{
        HashSet<PolicyMapMutator> mutators = new HashSet<PolicyMapMutator>();
        PolicyMapExtender myExtender = PolicyMapExtender.createPolicyMapExtender();
        mutators.add(myExtender);
        if(null!=externalMutators) {
            mutators.addAll(externalMutators);
        }
        PolicyMap policyMap = PolicyMap.createPolicyMap(mutators);
        for(BuilderHandler builder : getPolicyBuilders()){
            builder.populate(myExtender);
        }
        return policyMap;
    }
    
    /**
     * private policyBuilder getter method
     */
    private List<BuilderHandler> getPolicyBuilders(){
        if(null==this.policyBuilders)
            this.policyBuilders = new LinkedList<BuilderHandler>();
        return this.policyBuilders;
    }
    
    void unregisterAll() {
        this.policyBuilders = null;
    }

}
