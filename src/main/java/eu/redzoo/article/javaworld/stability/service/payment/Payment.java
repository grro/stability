/*
 * Copyright (c) 2014, Gregor Roth, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 */
package eu.redzoo.article.javaworld.stability.service.payment;

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
