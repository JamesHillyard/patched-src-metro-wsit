/*
 * $Id: Attribute.java,v 1.1 2006-05-03 22:58:13 arungupta Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AttributeType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The <code>Attribute</code> element specifies an attribute of the assertion subject.
 * The <code>Attribute</code> element is an extension of the <code>AttributeDesignator</code> element
 * that allows the attribute value to be specified.
 */
public class Attribute extends AttributeType
    implements com.sun.xml.wss.saml.Attribute {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    /**
     * Constructs an attribute element from an existing XML block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AttributeType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AttributeType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    private void setAttributeValue(List values) {
        this.attributeValue = values;
    }
    
    /**
     * Constructs an instance of <code>Attribute</code>.
     *
     * @param name A String representing <code>AttributeName</code> (the name
     *        of the attribute).
     * @param nameSpace A String representing the namespace in which
     *        <code>AttributeName</code> elements are interpreted.
     * @param values A List of DOM element representing the
     *        <code>AttributeValue</code> object.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public Attribute(String name, String nameSpace, List values) {
        setAttributeName(name);
        setAttributeNamespace(nameSpace);
        setAttributeValue(values);
    }
}
