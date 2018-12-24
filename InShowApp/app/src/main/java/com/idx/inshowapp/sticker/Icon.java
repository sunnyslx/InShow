package com.idx.inshowapp.sticker;

/**
 * Created by stefan on 18-10-27.
 */

public class Icon {
    private int resourceId;
    private int stickerResourceId;
    private String name;

    public Icon(int resourceId,int stickerResourceId){
        this.resourceId = resourceId;
        this.stickerResourceId = stickerResourceId;
    }

    public Icon(int resourceId, String name){
        this.resourceId = resourceId;
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public int getStickerResourceId() {
        return stickerResourceId;
    }

    public String getName(){
        return name;
    }
}
