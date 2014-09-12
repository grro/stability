package eu.redzoo.article.javaworld.reliable.payment;

import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement
public class Payment {

    private String id;
    private boolean isDelayed;
    

    public Payment() {
    }
    
    
    public Payment(String id, boolean isDelayed) {
        this.id = id;
        this.isDelayed = isDelayed;
    }
    
    
    public void setId(String id) {
        this.id = id;
    }

    public void setDelayed(boolean isDelayed) {
        this.isDelayed = isDelayed;
    }


    public String getId() {
        return id;
    }
    
    public boolean isDelayed() {
        return isDelayed;
    }
}
