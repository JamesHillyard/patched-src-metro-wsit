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

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class Invocation {
    private Method method;
    private Object[] arguments;
    private String argsString;
    
    public static Invocation createInvocation(Method method, Object[] args) {
        Object[] arguments;
        if (args != null && args.length == 3 && "writeCharacters".equals(method.getName())) {
            Integer start = (Integer) args[1];
            Integer length = (Integer) args[2];
            char[] text = new char[length.intValue()];
            System.arraycopy(args[0], start, text, 0, length);
            
            arguments = new Object[3];
            arguments[0] = text;
            arguments[1] = Integer.valueOf(0);
            arguments[2] = length;
        } else {
            arguments = args;
        }
        
        return new Invocation(method, arguments);
    }
    
    private Invocation(Method method, Object[] args) {
        this.method = method;
        this.arguments = args;
    }
    
    public String getMethodName() {
        return method.getName();
    }
    
    public Object getArgument(int index) {
        return arguments[index];
    }
    
    public int getArgumentsLength() {
        return (arguments != null) ? arguments.length : 0;
    }
    
    public Object execute(Object target) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(target, arguments);
    }
    
    public String toString() {
        StringBuffer retValue = new StringBuffer("invocation {");
        retValue.append("method='").append(method.getName()).append("', args=").append(argsToString());
        retValue.append('}');
        
        return retValue.toString();
    }
    
    public String argsToString() {
        if (argsString == null) {
            List argList = null;
            if (arguments != null && arguments.length > 0) {
                if (arguments.length == 3 && "writeCharacters".equals(method.getName())) {
                    argList = new ArrayList(3);
                    argList.add(new String((char[]) arguments[0]));
                    argList.add(arguments[1]);
                    argList.add(arguments[2]);
                } else {
                    argList = Arrays.asList(arguments);
                }
            }
            argsString = (argList != null) ? argList.toString() : "no arguments";
        }
        
        return argsString;
    }
}
