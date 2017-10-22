package org.voidptr.swpieview;

import android.media.Image;
import android.net.Uri;

/**
 * Created by errant on 3/25/17.
 */

public class ImageContainer {
    public ImageContainer(){
    }

    public ImageContainer(Uri imageUri){
        uri = imageUri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    private Uri uri;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private String mimeType;
}
