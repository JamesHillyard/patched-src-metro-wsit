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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.wss.impl.MessageConstants;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.transform.Source;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;

//import com.sun.xml.security.core.xenc.EncryptedDataType;
import com.sun.xml.wss.SubjectAccessor;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;

import com.sun.xml.ws.security.wsu.AttributedDateTime;

import com.sun.xml.wss.core.reference.X509ThumbPrintIdentifier;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.saml.Advice;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AttributeStatement;
import com.sun.xml.wss.saml.Conditions;
import com.sun.xml.wss.saml.NameIdentifier;
import com.sun.xml.wss.saml.SAMLAssertionFactory;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.saml.SubjectConfirmation;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType;

import javax.security.auth.Subject;

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public  class IssueSamlTokenContractImpl extends IssueSamlTokenContract {

    private static final String SAML_HOLDER_OF_KEY = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
   
    protected Token createSAMLAssertion(byte[] key, String assertionId, String appliesTo) throws WSTrustException
    {       
        Token token = null;
        
        // get authenticaed client Subject
        Subject subject = SubjectAccessor.getRequesterSubject();
        Set<Principal> principals = subject.getPrincipals();
        String tokenType = WSTrustConstants.SAML11_ASEERTION_TOKEN_TYPE;
        if (!isAuthorized(subject, appliesTo, tokenType)){
            throw new WSTrustException("The client is not authorized to be issued the token of type "+ tokenType + " apply to " + appliesTo);
        }
        
        try{
            CallbackHandler callbackHandler = config.getCallbackHandler();
            //CallbackHandler callbackHandler = (CallbackHandler)Class.forName(config.getCallbackHandlerName(),
                                 // true, Thread.currentThread().getContextClassLoader()).newInstance();

            
            // Get the service certificate and the corresponding public key
            EncryptionKeyCallback.AliasX509CertificateRequest req = new EncryptionKeyCallback.AliasX509CertificateRequest(config.getCertAlias());
            EncryptionKeyCallback ec = new EncryptionKeyCallback(req);
            Callback[] callbacks = {ec};
            callbackHandler.handle(callbacks);
            X509Certificate serCert = req.getX509Certificate();
            PublicKey serPubKey = serCert.getPublicKey();
            XMLCipher cipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
            cipher.init(XMLCipher.WRAP_MODE, serPubKey);
 
            SAMLAssertionFactory samlFac = 
                   SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
            String issuer = "TestSTS";
            GregorianCalendar issuerInst = new GregorianCalendar(); 
            GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)config.getIssuedTokenTimeout());

            Conditions conditions = 
                 samlFac.createConditions(issuerInst, notOnOrAfter, null, null, null);
            Advice advice = samlFac.createAdvice(null, null, null);

            List confirmationMethods = new ArrayList();
            confirmationMethods.add(SAML_HOLDER_OF_KEY);
           
            // Create KeyInfo 
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            Document doc = docFactory.newDocumentBuilder().newDocument();
            KeyInfo keyInfo = new KeyInfo(doc);
            if (!config.getEncryptIssuedToken() && config.getEncryptIssuedKey()){
                // Encrypt the secret key and create EncryptedKey 
                EncryptedKey encKey = cipher.encryptKey(doc, new SecretKeySpec(key, "AES"));
                KeyInfo keyinfo = new KeyInfo(doc);
                KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.ThumbPrintIdentifier_NS,null);
                keyIdentifier.setValue(Base64.encode(X509ThumbPrintIdentifier.getThumbPrintIdentifier(serCert)));
                SecurityTokenReference str = new SecurityTokenReferenceImpl(keyIdentifier);
                keyinfo.addUnknownElement(WSTrustElementFactory.newInstance().toElement(str,doc));
                encKey.setKeyInfo(keyinfo);
                keyInfo.add(encKey);
            }
            else{
                BinarySecret bs = eleFac.createBinarySecret(key, BinarySecret.SYMMETRIC_KEY_TYPE);
                keyInfo.addUnknownElement(eleFac.toElement(bs,doc));
            }
           
            SubjectConfirmation subjectConfirmation = samlFac.createSubjectConfirmation(
            confirmationMethods, null, keyInfo.getElement());

            NameIdentifier nameId = null;
            List claimedAttrs = getClaimedAttributes(subject, appliesTo, tokenType);
            if (!claimedAttrs.isEmpty()){
                String name = (String)claimedAttrs.get(0);
                nameId = samlFac.createNameIdentifier(name, null, null);
            }
            com.sun.xml.wss.saml.Subject subj = samlFac.createSubject(nameId, subjectConfirmation);
            AttributeStatement statement = samlFac.createAttributeStatement(subj, null);
            List statements = new ArrayList();
            statements.add(statement);
            Assertion assertion = 
                   samlFac.createAssertion(assertionId, issuer, issuerInst, conditions, advice, statements);

            // Get the STS's public and private key 
            SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            callbacks[0] = new SignatureKeyCallback(request);
            callbackHandler.handle(callbacks);                                                                                      
            PublicKey stsPubKey = request.getX509Certificate().getPublicKey();
            PrivateKey stsPrivKey = request.getPrivateKey();
            
            // Sign the assertion with STS's private key
            Element signedAssertion = assertion.sign(stsPubKey, stsPrivKey);
            
            //javax.xml.bind.Unmarshaller u = eleFac.getContext().createUnmarshaller();
            //JAXBElement<AssertionType> aType = u.unmarshal(signedAssertion, AssertionType.class);
            //assertion =  new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion(aType.getValue());
            token = new GenericToken(signedAssertion);
            
            if (config.getEncryptIssuedToken()){
                // Encrypt the assertion and return the Encrypteddata
                EncryptedData encData = cipher.encryptData(signedAssertion.getOwnerDocument(), signedAssertion);
                String id = "uuid-" + UUID.randomUUID().toString();
                encData.setId(id);
                token = new GenericToken(cipher.martial(encData));
                //JAXBElement<EncryptedDataType> eEle = u.unmarshal(cipher.martial(encData), EncryptedDataType.class);
                //return eEle.getValue();
            }else{
                token = new GenericToken(signedAssertion);
            }
       
        } catch (XWSSecurityException ex){
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        } catch (SAMLException ex) {
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        }
        catch (XMLEncryptionException ex) {
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        }
        catch (JAXBException ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        }
       
        return token;
   }
    
   protected boolean isAuthorized(Subject subject, String appliesTo, String tokenType){
       return true;
   }
   
   protected List getClaimedAttributes(Subject subject, String appliesTo, String tokenType){
       Set<Principal> principals = subject.getPrincipals();
       List attrs = new ArrayList();
       if (principals != null){
           Iterator iterator = principals.iterator();
           while (iterator.hasNext()){
                String name = principals.iterator().next().getName();
                if (name != null){
                    attrs.add(name);
                    break;
                }
           }       
       }
       return attrs;
   }
}
