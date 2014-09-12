package eu.redzoo.article.javaworld.reliable.payment;

import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement
public enum PaymentMethod {
 
    CREDITCARD, PAYPAL, PREPAYMENT, INVOCE;
}
