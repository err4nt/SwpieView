package org.voidptr.swpieview;

import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by errant on 3/25/17.
 */

public class ImageContainer implements Comparable<ImageContainer> {
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

    @Override
    public int compareTo(@NonNull ImageContainer imageContainer) {
        List<String> thisPathParts = uri.getPathSegments();
        String thisPath = thisPathParts.get(3);
        String thisFilename = thisPath.substring(thisPath.lastIndexOf("/")+1, thisPath.length());

        List<String> thatPathParts = imageContainer.getUri().getPathSegments();
        String thatPath = thatPathParts.get(3);
        String thatFilename = thatPath.substring(thatPath.lastIndexOf("/")+1, thatPath.length());

        return thisFilename.compareTo(thatFilename);
    }
}
