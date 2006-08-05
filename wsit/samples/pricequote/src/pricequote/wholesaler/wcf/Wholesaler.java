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
 $Id: Wholesaler.java,v 1.2 2006-08-05 00:29:37 arungupta Exp $
*/

package pricequote.wholesaler.wcf;

import java.io.InputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.jws.WebService;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.servlet.ServletContext;
import javax.imageio.ImageIO;

/**
 * @author Arun Gupta
 */
@WebService(endpointInterface = "pricequote.wholesaler.wcf.WholesalerPortType", wsdlLocation = "WEB-INF/wsdl/wholesaler.wsdl")
public class Wholesaler implements WholesalerPortType {

    @Resource
    WebServiceContext context;

    public Quote getQuote(int i) {
        Quote response = new Quote();
        response.setPrice(Wholesaler.PRICES[i % 4]);

        ServletContext servletContext = (ServletContext)context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        if (servletContext != null) {

            String carName = "/images/" + carname(i) + ".jpg";

            System.out.println("Car name is: " + carName);
            InputStream is = servletContext.getResourceAsStream(carName);
            try {
                BufferedImage bi = ImageIO.read(is);
                response.setPhoto(bi);
                System.out.println(getClass().getName() + ": Added the photo");
            } catch (IOException e) {
                throw new WebServiceException(e);
            }
        }

        return response;
    }

    private String carname(int pid) {
        switch (pid % 4) {
            case 1:
                return "AM-Vantage-2k6";
            case 2:
                return "BMW-M3-2k6";
            case 3:
                return "MB-SLR-2k6";
            case 0:
            default:
                return "Porsche-911-2k6";
        }
    }

    private static final float[] PRICES = {
        (float)71834.95,
        (float)83450.00,
        (float)75640.00,
        (float)90990.99
    };
}
