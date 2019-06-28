package com.mycompany.mvpnews.db;

import org.litepal.crud.DataSupport;

public class TopNews extends DataSupport {

    private String newId;

    private String newTitle;

    private String newImage;

    private String newBody;

    private String newCss;

    private String newJS;

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public String getNewImage() {
        return newImage;
    }

    public void setNewImage(String newImage) {
        this.newImage = newImage;
    }

    public String getNewBody() {
        return newBody;
    }

    public void setNewBody(String newBody) {
        this.newBody = newBody;
    }

    public String getNewCss() {
        return newCss;
    }

    public void setNewCss(String newCss) {
        this.newCss = newCss;
    }

    public String getNewJS() {
        return newJS;
    }

    public void setNewJS(String newJS) {
        this.newJS = newJS;
    }
}
