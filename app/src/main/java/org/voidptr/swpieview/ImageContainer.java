package org.voidptr.swpieview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by errant on 3/25/17.
 */

public class ImageContainer implements Comparable<ImageContainer>, Parcelable {
    private Bitmap thumbnail;

    ImageContainer(){
    }

    private ImageContainer(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        mimeType = in.readString();
    }

    public static final Creator<ImageContainer> CREATOR = new Creator<ImageContainer>() {
        @Override
        public ImageContainer createFromParcel(Parcel in) {
            return new ImageContainer(in);
        }

        @Override
        public ImageContainer[] newArray(int size) {
            return new ImageContainer[size];
        }
    };

    Uri getUri() {
        return uri;
    }

    void setUri(Uri uri) {
        this.uri = uri;
    }

    private Uri uri;

    String getMimeType() {
        return mimeType;
    }

    void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private String mimeType;

    @Override
    public int compareTo(@NonNull ImageContainer imageContainer) {
        List<String> thisPathParts = uri.getPathSegments();
        String thisPath = thisPathParts.get(3);
        String thisFilename = thisPath.substring(thisPath
                .lastIndexOf("/")+1, thisPath.length());

        List<String> thatPathParts = imageContainer.getUri().getPathSegments();
        String thatPath = thatPathParts.get(3);
        String thatFilename = thatPath.substring(thatPath
                .lastIndexOf("/")+1, thatPath.length());

        return thisFilename.compareTo(thatFilename);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(mimeType);
    }

    Bitmap getThumbnail(Context context) {
        if(thumbnail == null) {
            try {
                InputStream stream = context.getContentResolver().openInputStream(getUri());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                thumbnail = BitmapFactory.decodeStream(stream,
                        new Rect(0, 0, 0, 0), options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return  thumbnail;
    }
}
