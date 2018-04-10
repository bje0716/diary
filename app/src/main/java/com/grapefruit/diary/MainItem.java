package com.grapefruit.diary;

import io.realm.RealmObject;

public class MainItem extends RealmObject {

    private long date;
    private String content;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
