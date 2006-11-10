package xwss.s11.server;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;


public class PlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {


        public boolean validate(PasswordValidationCallback.Request request)
            throws PasswordValidationCallback.PasswordValidationException {
            System.out.println("Using configured PlainTextPasswordValidator................");
            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                (PasswordValidationCallback.PlainTextPasswordRequest) request;
            if ("Alice".equals(plainTextRequest.getUsername()) &&
                "ecilA".equals(plainTextRequest.getPassword())) {
                return true;
            }
            return false;
        }
}
