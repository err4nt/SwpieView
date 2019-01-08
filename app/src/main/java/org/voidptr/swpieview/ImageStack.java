package org.voidptr.swpieview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Represents the current stack of images being displayed
 */

public class ImageStack extends BaseAdapter {
    private ArrayList<ImageContainer> stack;
    private Integer index;
    private Context context;

    /**
     * Create a new empty <code>ImageStack</code>
     */
    ImageStack(Context context) {
        stack = new ArrayList<>();
        index = 0;
        this.context = context;
    }

    ImageStack(Context context, Bundle fromBundle) {
        this.context = context;
        this.stack = fromBundle.getParcelableArrayList("stack");
        this.index = fromBundle.getInt("index");
    }

    /**
     * Get the current list of <code>ImageContainer</code> objects
     *
     * @return List of <code>ImageContainer</code> objects
     */
    public ArrayList<ImageContainer> getStack() {
        return stack;
    }

    /**
     * Set the current list of images and set index to 0
     *
     * @param stack List of <code>ImageContainer</code> objects
     */
    void setStack(ArrayList<ImageContainer> stack) {
        this.stack = stack;
        index = 0;
    }

    /**
     * Go to the next image in the list and return it
     *
     * @return <code>ImageContainer</code>
     */
    ImageContainer next() {
        if (index < stack.size()) {
            index++;
        }
        return stack.get(index);
    }

    /**
     * Go to the previous image in the list and return it
     *
     * @return <code>ImageContainer</code>
     */
    ImageContainer previous() {
        if (index > 0) {
            index--;
        }
        return stack.get(index);
    }

    /**
     * Go to the first image in the list and return it
     *
     * @return <code>ImageContainer</code>
     */
    public ImageContainer first() {
        index = 0;
        return stack.get(index);
    }

    /**
     * Return true if there are more images in the list
     *
     * @return <code>boolean</code>
     */
    boolean hasNext() {
        return (index < stack.size() - 1);
    }

    /**
     * Return true if there are images before the current image
     *
     * @return <code>boolean</code>
     */
    boolean hasPrevious() {
        return (index >= 0);
    }

    void setIndex(Integer index) {
        this.index = index;
    }

    ImageContainer getCurrent() {
        return stack.get(index);
    }

    /**
     * Go to a specific image. Note that selected instance must come from the stack.
     *
     * @param selected <code>ImageContainer</code>
     */
    public void selectImage(ImageContainer selected) {
        index = stack.indexOf(selected);
    }

    Bundle toBundle() {
        Bundle out = new Bundle();

        out.putParcelableArrayList("stack", stack);
        out.putInt("index", index);

        return out;
    }

    @Override
    public int getCount() {
        return stack.size();
    }

    @Override
    public Object getItem(int position) {
        return stack.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        ImageContainer container = stack.get(position);
        imageView.setImageBitmap(container.getThumbnail(context));

        return imageView;
    }
}
