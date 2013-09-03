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

package com.sun.xml.wss.impl.misc;

import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.wss.core.reference.SamlKeyIdentifier;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import javax.xml.bind.JAXBElement;

import javax.xml.soap.SOAPElement;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;

import java.util.Random;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.wss.impl.WSSAssertion;

import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;


import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;

import com.sun.xml.ws.security.SecurityTokenReference;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;

import com.sun.xml.ws.security.trust.WSTrustConstants;

import com.sun.xml.ws.security.SecurityContextToken;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.security.opt.impl.keyinfo.SecurityContextToken13;
import com.sun.xml.ws.security.secconv.impl.bindings.SecurityContextTokenType;
import com.sun.xml.ws.security.secconv.impl.client.DefaultSCTokenConfiguration;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Utility class for the Encryption and Signature related methods
 * @author Ashutosh Shahi
 */
public class SecurityUtil {
    
    protected static final Logger log =  Logger.getLogger( LogDomainConstants.IMPL_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_CRYPTO_DOMAIN_BUNDLE);
    
    /** Creates a new instance of SecurityUtil */
    public SecurityUtil() {
    }
    
    public static SecretKey generateSymmetricKey(String algorithm) throws XWSSecurityException{
        try {
            
            //keyGen.init(168);//TODO-Venu
            String jceAlgo = JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);
            //JCEMapper.translateURItoJCEID(algorithm);
            //
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "JCE ALGORITHM "+ jceAlgo);
            }
            KeyGenerator keyGen = KeyGenerator.getInstance(jceAlgo);
            int length = 0;
            if(jceAlgo.startsWith("DES")){
                length = 168;
            }else{
                length = JCEMapper.getKeyLengthFromURI(algorithm);
            }
            keyGen.init(length);
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "Algorithm key length "+length);
            }
            return keyGen.generateKey();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "WSS1208.failedto.generate.random.symmetrickey",
                    new Object[] {e.getMessage()});
            throw new XWSSecurityException(
                    "Unable to Generate Symmetric Key", e);
        }
    }
    
    /**
     * Lookup method to get the Key Length based on algorithm
     * TODO: Not complete yet, need to add more algorithms
     * NOTE: This method should only be used for DerivedKeyTokenLengths
     **/
    public static int getLengthFromAlgorithm(String algorithm) throws XWSSecurityException{
        if(algorithm.equals(MessageConstants.AES_BLOCK_ENCRYPTION_192)){
            return 24;
        } else if(algorithm.equals(MessageConstants.AES_BLOCK_ENCRYPTION_256)){
            return 32;
        } else if(algorithm.equals(MessageConstants.AES_BLOCK_ENCRYPTION_128)){
            return 16;
        } else if (algorithm.equals(MessageConstants.TRIPLE_DES_BLOCK_ENCRYPTION)) {
            return 24;
        } else {
            throw new UnsupportedOperationException("TODO: not yet implemented keyLength for" + algorithm);
        }
    }
    
    public static String generateUUID() {
        Random rnd = new Random();
        int intRandom = rnd.nextInt();
        String id = "XWSSGID-"+String.valueOf(System.currentTimeMillis())+String.valueOf(intRandom);
        return id;
    }
    
    public static byte[] P_SHA1(byte[] secret, byte[] seed
            ) throws Exception {
        
        byte[] aBytes, result;
        aBytes = seed;
        
        Mac hMac = Mac.getInstance("HMACSHA1");
        SecretKeySpec sKey = new SecretKeySpec(secret, "HMACSHA1");
        hMac.init(sKey);
        hMac.update(aBytes);
        aBytes = hMac.doFinal();
        hMac.reset();
        hMac.init(sKey);
        hMac.update(aBytes);
        hMac.update(seed);
        result = hMac.doFinal();
        
        return result;
    }
    
    public static byte[] P_SHA1(byte[] secret, byte[] seed,
            int requiredSize) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hMac = Mac.getInstance("HMACSHA1");
        SecretKeySpec sKey = new SecretKeySpec(secret, "HMACSHA1");
        
        byte[] result = new byte[requiredSize];
        int copied=0;
        
        byte[] aBytes = seed;
        hMac.init(sKey);
        hMac.update(aBytes);
        aBytes  = hMac.doFinal();
        
        int rounds = requiredSize/aBytes.length ;
        if(requiredSize % aBytes.length != 0)
            rounds++;
        
        for(int i = 0; i < rounds; i ++){
            
            hMac.reset();
            hMac.init(sKey);
            hMac.update(aBytes);
            hMac.update(seed);
            byte[] generated = hMac.doFinal();
            int takeBytes;
            if(i != rounds-1)
                takeBytes = generated.length;
            else
                takeBytes = requiredSize - (generated.length * i);
            System.arraycopy(generated, 0, result, copied, takeBytes);
            copied += takeBytes;
            hMac.init(sKey);
            hMac.update(aBytes);
            aBytes  = hMac.doFinal();
        }
        return result;
    }
    
    public static String getSecretKeyAlgorithm(String encryptionAlgo) {
        String encAlgo = JCEMapper.translateURItoJCEID(encryptionAlgo);
        if (encAlgo.startsWith("AES")) {
            return "AES";
        } else if (encAlgo.startsWith("DESede")) {
            return "DESede";
        } else if (encAlgo.startsWith("DES")) {
            return "DES";
        }
        return encAlgo;
    }
    
    public static void checkIncludeTokenPolicy(FilterProcessingContext context,
            AuthenticationTokenPolicy.X509CertificateBinding certInfo,
            String x509id) throws XWSSecurityException{
        
        HashMap insertedX509Cache = context.getInsertedX509Cache();
        X509SecurityToken x509Token = (X509SecurityToken)insertedX509Cache.get(x509id);
        //SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        
        try{
            if(x509Token == null){
                /*Token policyToken = certInfo.getPolicyToken();
                if (policyToken == null) {
                    return;
                }*/
                // no referencetype adjustment if it is not WS-SecurityPolicy
                if (!certInfo.policyTokenWasSet()){
                    return;
                }
                
                if(certInfo.INCLUDE_ALWAYS_TO_RECIPIENT.equals(certInfo.getIncludeToken()) ||
                        certInfo.INCLUDE_ALWAYS.equals(certInfo.getIncludeToken())){
                    insertCertificate(context, certInfo, x509id);
                } else if(certInfo.INCLUDE_NEVER.equals(certInfo.getIncludeToken())){
                    WSSAssertion wssAssertion = context.getWSSAssertion();
                    if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(certInfo.getReferenceType())){
                        if(wssAssertion != null){
                            if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUST_SUPPORT_REF_KEYIDENTIFIER))
                                certInfo.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                            else if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUSTSUPPORT_REF_THUMBPRINT))
                                certInfo.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
                        } else {
                            // when wssAssertion is not set use KeyIdentifier
                            certInfo.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        }
                    }
                } else if(certInfo.INCLUDE_ONCE.equals(certInfo.getIncludeToken())){
                    throw new UnsupportedOperationException(certInfo.INCLUDE_ONCE + " not supported yet as IncludeToken policy");
                }
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
    public static void checkIncludeTokenPolicyOpt(JAXBFilterProcessingContext context,
            AuthenticationTokenPolicy.X509CertificateBinding certInfo,
            String x509id) throws XWSSecurityException{
        
        //SecurityHeaderElement she = context.getSecurityHeader().getChildElement(x509id);
        
        try{
            //if(she != null){
                /*Token policyToken = certInfo.getPolicyToken();
                if (policyToken == null) {
                    return;
                }*/
            // no referencetype adjustment if it is not WS-SecurityPolicy
            if (!certInfo.policyTokenWasSet()){
                return;
            }
            if(certInfo.INCLUDE_ALWAYS_TO_RECIPIENT.equals(certInfo.getIncludeToken()) ||
                    certInfo.INCLUDE_ALWAYS.equals(certInfo.getIncludeToken())){
                certInfo.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
            } else if(certInfo.INCLUDE_NEVER.equals(certInfo.getIncludeToken())){
                WSSAssertion wssAssertion = context.getWSSAssertion();
                if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(certInfo.getReferenceType())){
                    if(wssAssertion != null){
                        if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUST_SUPPORT_REF_KEYIDENTIFIER))
                            certInfo.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        else if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUSTSUPPORT_REF_THUMBPRINT))
                            certInfo.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
                    } else {
                        // when wssAssertion is not set use KeyIdentifier
                        certInfo.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    }
                }
            } else if(certInfo.INCLUDE_ONCE.equals(certInfo.getIncludeToken())){
                throw new UnsupportedOperationException(certInfo.INCLUDE_ONCE + " not supported yet as IncludeToken policy");
            }
            //}
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
    public static String  getWsuIdOrId(Element elem) throws XWSSecurityException {
        NamedNodeMap nmap = elem.getAttributes();
        Node attr = nmap.getNamedItem("Id");
        if (attr == null) {
            attr = nmap.getNamedItem("AssertionID");
            if (attr == null)
                attr = nmap.getNamedItem("ID");
            if (attr == null) {
                throw new XWSSecurityException("Issued Token Element does not have a Id or AssertionId attribute");
            }
        }
        return attr.getNodeValue();
    }
    
    
    public static void resolveSCT(FilterProcessingContext context, SecureConversationTokenKeyBinding sctBinding)
    throws XWSSecurityException {
        // resolve the ProofKey here and set it into ProcessingContext
        //String sctPolicyId = sctBinding.getPolicyToken().getTokenId();
        String sctPolicyId = sctBinding.getUUID();        
        // this will work on the client side only
        //IssuedTokenContext ictx = context.getIssuedTokenContext(sctPolicyId);
        IssuedTokenContext ictx = null;
        if(context.isClient()){            
            String sctId = context.getSCPolicyIDtoSctIdMap(sctPolicyId);
            String protocol = context.getWSSCVersion(context.getSecurityPolicyVersion());
            SCTokenConfiguration config = new DefaultSCTokenConfiguration(protocol, sctId, !context.isExpired(), !context.isInboundMessage());
            ictx =IssuedTokenManager.getInstance().createIssuedTokenContext(config, null);
            try{
                IssuedTokenManager.getInstance().getIssuedToken(ictx);
            }catch(WSTrustException e){
                throw new XWSSecurityException(e);
            }
        }
        if (ictx == null) {
            // this will work on the server side
            String sctId = "";
            if(context instanceof JAXBFilterProcessingContext){
                
                Object sctObject = context.getExtraneousProperty(MessageConstants.INCOMING_SCT);
                
                if (sctObject == null) {
                    throw new XWSSecurityException("SecureConversation Session Context not Found");
                }
                if(sctObject instanceof com.sun.xml.ws.security.opt.impl.incoming.SecurityContextToken){
                    com.sun.xml.ws.security.opt.impl.incoming.SecurityContextToken sct = (com.sun.xml.ws.security.opt.impl.incoming.SecurityContextToken)sctObject;
                    sctId = sct.getSCId();
                }else if(sctObject instanceof SecurityContextToken){
                    SecurityContextToken sct = (SecurityContextToken)sctObject;
                    sctId = sct.getIdentifier().toString();
                }                                                                
            } else{                                
                SecurityContextToken sct = (SecurityContextToken)context.getExtraneousProperty(MessageConstants.INCOMING_SCT);
                if (sct == null) {
                    throw new XWSSecurityException("SecureConversation Session Context not Found");
                }                
                sctId = sct.getIdentifier().toString();
            }
            
            ictx = SessionManager.getSessionManager().getSecurityContext(sctId, !context.isExpired());
        }
        
        if (ictx == null) {
            throw new XWSSecurityException("SecureConversation Session Context not Found");
        } else {
            //System.out.println("SC Session located...");            
        }
        //TODO: assuming only a single secure-conversation context
        context.setSecureConversationContext(ictx);
    }
    
    public static void resolveIssuedToken(FilterProcessingContext context, IssuedTokenKeyBinding itkb) throws XWSSecurityException {
        //resolve the ProofKey here and set it into ProcessingContext
        //String itPolicyId = itkb.getPolicyToken().getTokenId();
        String itPolicyId = itkb.getUUID();
        // this will work on the client side only
        IssuedTokenContext ictx = context.getIssuedTokenContext(itPolicyId);
        boolean clientSide = true;
        if (ictx == null) {
            // on the server we have the TrustCredentialHolder
            ictx = context.getTrustCredentialHolder();
            clientSide = false;
        }
        
        if (ictx == null) {
            throw new XWSSecurityException("Trust IssuedToken not Found");
        }
        if (ictx.getSecurityToken() instanceof GenericToken) {
            itkb.setRealId(((GenericToken)ictx.getSecurityToken()).getId());
        }

        context.setTrustContext(ictx);
        if (ictx.getProofKey() == null){
            //handle asymmetric issued key
            if (clientSide) {
                //TODO: change this later to use the Cert Alias
                X509Certificate cert = context.getSecurityEnvironment().getDefaultCertificate(
                        context.getExtraneousProperties());
                ictx.setRequestorCertificate(cert);
            }  else {
                //nothing todo on server side
            }
        }
    }
    
    public static void initInferredIssuedTokenContext(FilterProcessingContext wssContext, Token str, Key returnKey) throws XWSSecurityException {
        // new code which fixes issues with Brokered Trust.
        IssuedTokenContextImpl ictx = (IssuedTokenContextImpl)wssContext.getTrustCredentialHolder();
        if (ictx == null) {
            ictx = new IssuedTokenContextImpl();
        }
        
        ictx.setProofKey(returnKey.getEncoded());
        ictx.setUnAttachedSecurityTokenReference(str);
        wssContext.setTrustCredentialHolder(ictx);
    }
    
    public static boolean isEncryptedKey(SOAPElement elem) {
        
        if (MessageConstants.XENC_ENCRYPTED_KEY_LNAME.equals(elem.getLocalName()) &&
                MessageConstants.XENC_NS.equals(elem.getNamespaceURI())) {
            return true;
        }
        return false;
    }
    
    public static boolean isBinarySecret(SOAPElement elem) {
        if (MessageConstants.BINARY_SECRET_LNAME.equals(elem.getLocalName()) &&
                WSTrustConstants.WST_NAMESPACE.equals(elem.getNamespaceURI())) {
            return true;
        }
        return false;
    }
    
    public static SecurityContextTokenImpl locateBySCTId(FilterProcessingContext context, String sctId) throws XWSSecurityException {
        
        Hashtable contextMap = context.getIssuedTokenContextMap();
        
        if (contextMap == null) {
            // print a warning here
            //System.out.println("context.getIssuedTokenContextMap was null.........");
            return null;
        }
        
        Iterator<Map.Entry> it = contextMap.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            String tokenId = (String)entry.getKey();
            Object token = entry.getValue();
            if (token instanceof IssuedTokenContext) {
                Object securityToken = ((IssuedTokenContext)token).getSecurityToken();
                if (securityToken instanceof SecurityContextToken) {
                    SecurityContextToken ret = (SecurityContextToken)securityToken;
                    if (sctId.equals(ret.getIdentifier().toString())) {
                        return new SecurityContextTokenImpl(
                                context.getSOAPMessage().getSOAPPart(), ret.getIdentifier().toString(), ret.getInstance(), ret.getWsuId(), ret.getExtElements());
                    }
                }
            }
        }
        return null;
    }
    
    public static void updateSamlVsKeyCache(SecurityTokenReference str, FilterProcessingContext ctx, Key symKey) {
        com.sun.xml.wss.core.ReferenceElement ref = ((com.sun.xml.wss.core.SecurityTokenReference)str).getReference();
        if (ref instanceof com.sun.xml.wss.core.reference.KeyIdentifier) {
            String assertionId = ((com.sun.xml.wss.core.reference.KeyIdentifier)ref).getReferenceValue();
            if (ctx.getSamlIdVSKeyCache().get(assertionId) == null) {
                ctx.getSamlIdVSKeyCache().put(assertionId, symKey);
            }
        }
    }
    
    public static void updateSamlVsKeyCache(SecurityTokenReferenceType str, FilterProcessingContext ctx, Key symKey) {
        List<Object> list = str.getAny();
        for(int i=0;i<list.size();i++){
            Object item = list.get(i);
            if(item instanceof JAXBElement){
                item = ((JAXBElement)item).getValue();
            }
            if(item instanceof com.sun.xml.ws.security.secext10.KeyIdentifierType){
                String assertionId = ((com.sun.xml.ws.security.secext10.KeyIdentifierType)item).getValue();
                if (ctx.getSamlIdVSKeyCache().get(assertionId) == null) {
                    ctx.getSamlIdVSKeyCache().put(assertionId, symKey);
                }
                HashMap sentSamlKeys = (HashMap) ctx.getExtraneousProperty(MessageConstants.STORED_SAML_KEYS);
                if(sentSamlKeys != null){
                    if(sentSamlKeys.get(assertionId) == null){
                        sentSamlKeys.put(assertionId,symKey);
                    }
                }
            }
        }
    }
    
    public static void insertCertificate(FilterProcessingContext context,
            AuthenticationTokenPolicy.X509CertificateBinding certInfo,
            String x509id) throws XWSSecurityException{
        HashMap insertedX509Cache = context.getInsertedX509Cache();
        try{
            /*if(context instanceof JAXBFilterProcessingContext){
                WSSElementFactory elementFactory = new WSSElementFactory();
                JAXBFilterProcessingContext opContext = (JAXBFilterProcessingContext)context;
                SecurityHeader secHeader = opContext.getSecurityHeader();
                byte[] cert = certInfo.getX509Certificate().getEncoded();
                BinarySecurityToken token = elementFactory.createBinarySecurityToken(x509id, cert);
                secHeader.add(token);
                insertedX509Cache.put(x509id, token);
                certInfo.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
            } else{*/
            String valueType = certInfo.getValueType();
            if(valueType==null||valueType.equals("")){
                //default valueType for X509 as v3
                valueType = MessageConstants.X509v3_NS;
            }
            SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
            X509SecurityToken x509Token = new X509SecurityToken(secureMessage.getSOAPPart(),certInfo.getX509Certificate(), x509id, valueType);
            secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(x509Token);
            insertedX509Cache.put(x509id, x509Token);
            certInfo.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
            //}
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
    
    public static String getDataEncryptionAlgo(JAXBFilterProcessingContext context){
        WSSPolicy policy = (WSSPolicy) context.getSecurityPolicy();
        String tmp = "";
        if(PolicyTypeUtil.encryptionPolicy(policy)){
            EncryptionPolicy.FeatureBinding featureBinding = (EncryptionPolicy.FeatureBinding) policy.getFeatureBinding();
            MLSPolicy keyBinding = ((EncryptionPolicy)policy).getKeyBinding();
            tmp = featureBinding.getDataEncryptionAlgorithm();
            if (PolicyTypeUtil.issuedTokenKeyBinding(keyBinding) && context.getTrustContext() != null) {
                tmp = context.getTrustContext().getEncryptWith();
            }
        }
        if (tmp == null || "".equals(tmp)) {
            if (context.getAlgorithmSuite() != null) {
                tmp = context.getAlgorithmSuite().getEncryptionAlgorithm();
            } else {
                // warn that no dataEncAlgo was set
            }
        }
        return tmp;
    }
    
    /**
     * Returns a URL pointing to the given config file. The file name is
     * looked up as a resource from a ServletContext.
     *
     * May return null if the file can not be found.
     *
     * @param configFileName The name of the file resource
     * @param context A ServletContext object. May not be null.
     */
    public static URL loadFromContext(final String configFileName, final Object context) {
        return ReflectionUtil.invoke(context, "getResource", URL.class, configFileName);
    }
    
    /**
     * Returns a URL pointing to the given config file. The file is looked up as
     * a resource on the classpath.
     *
     * May return null if the file can not be found.
     *
     * @param configFileName the name of the file resource. May not be {@code null}.
     */
    public static URL loadFromClasspath(final String configFileName) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResource(configFileName);
        } else {
            return cl.getResource(configFileName);
        }
    }
    
    public static Element convertSTRToElement(Object strElem, Document doc) throws XWSSecurityException{
        
        if(strElem == null || strElem instanceof Element){
            return (Element)strElem;
        }
        
        com.sun.xml.wss.core.SecurityTokenReference stRef = null;
        if(strElem instanceof com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier){
            com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier keyIdStrElem = (com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier)strElem;
            if(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyIdStrElem.getValueType())){
                stRef= new com.sun.xml.wss.core.SecurityTokenReference(doc);
                SamlKeyIdentifier keyId = new SamlKeyIdentifier(doc);
                keyId.setReferenceValue(keyIdStrElem.getReferenceValue());
                keyId.setValueType(keyIdStrElem.getValueType());
                stRef.setReference(keyId);
            } else {
                throw new XWSSecurityException("Unsupported reference type encountered");
            }
        }
        return stRef;
    }
    
    public static  void copySubject(final Subject to, final Subject from) {
         AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
               to.getPrincipals().addAll(from.getPrincipals());
               to.getPublicCredentials().addAll(from.getPublicCredentials());
               to.getPrivateCredentials().addAll(from.getPrivateCredentials());
               return null;
            }
        });
    }
    
    public static Subject getSubject(final Map context){
        Subject otherPartySubject =
                (Subject)context.get(MessageConstants.AUTH_SUBJECT);
        if (otherPartySubject != null) {
            return otherPartySubject;
        }
        otherPartySubject =
                (Subject) AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
                Subject otherPartySubj = new Subject();
                context.put(MessageConstants.AUTH_SUBJECT,otherPartySubj);
                return otherPartySubj;
            }
        }
        );
        return otherPartySubject;
    }
    
    public static SecurityContextToken getSCT(SecurityContextToken sct, SOAPVersion version){
        if(sct instanceof com.sun.xml.ws.security.secconv.impl.wssx.bindings.SecurityContextTokenType){
            return new SecurityContextToken13(
                    (com.sun.xml.ws.security.secconv.impl.wssx.bindings.SecurityContextTokenType)sct,version);
        }else{
            return new com.sun.xml.ws.security.opt.impl.keyinfo.SecurityContextToken((SecurityContextTokenType)sct,version);
        }        
    }
    
    public static  void copy(Map p1, Map p2) {
        if (p2 == null || p1 == null) {
            return;
        }
        p1.putAll(p2);
    }
    
     public static Object newInstance(String className,
            ClassLoader classLoader, String spiName) {
        try {
            Class spiClass;
            if (classLoader == null) {
                spiClass = Class.forName(className);
            } else {
                spiClass = classLoader.loadClass(className);
            }
            return spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new XWSSecurityRuntimeException(
                    "The "  +  spiName + " :"  + className + " specified in META-INF/services was not found", x);
        } catch (Exception x) {
            throw new XWSSecurityRuntimeException(
                    "The "  +  spiName + " :"  + className + " specified in META-INF/services could not be instantiated", x);
        }
    }
     
     public static  Object loadSPIClass(URL url, String spiName) {
        InputStream is = null;
        if (url == null) {
            return null;
        }
        try {
            is = url.openStream();
            if(is!=null) {
                try {
                    BufferedReader rd =
                            new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String factoryClassName = rd.readLine();
                    rd.close();
                    if (factoryClassName != null &&
                            ! "".equals(factoryClassName)) {
                        Object obj = newInstance(factoryClassName, Thread.currentThread().getContextClassLoader(), spiName);
                        return obj;
                    }
                } catch (Exception e) {
                    throw new XWSSecurityRuntimeException(e);
                }
            }
        } catch (IOException e) {
            return null;
        } 
        return null;
    }
     
    public static long toLong(String lng) throws XWSSecurityException {
        if (lng == null) {
            return 0;
        }
        Long ret = 0L;
        try {
            ret = Long.valueOf(lng);
        }catch (Exception e) {
            log.log(Level.SEVERE, "WSS0719.error.getting.longValue");
            throw new XWSSecurityException(e);
        }
        return ret; 
    }

}