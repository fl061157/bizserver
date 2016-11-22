package com.handwin.bean;

import java.io.Serializable;

/**
 * Created by fangliang on 16/7/6.
 */
public class LiveContent implements Serializable {

    private String type; //img text video audio //TODO Not Support richMedia

    private String content; //Or Bytes

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
