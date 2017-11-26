package org.voidptr.swpieview;

import java.util.ArrayList;

/**
 * Created by errant on 11/26/17.
 */

public class ImageStack {
    private ArrayList<ImageContainer> stack;
    private Integer index;

    public ImageStack(){
        stack = new ArrayList<>();
        index = 0;
    }

    public ArrayList<ImageContainer> getStack() {
        return stack;
    }

    public void setStack(ArrayList<ImageContainer> stack) {
        this.stack = stack;
        index = 0;
    }

    public ImageContainer next(){
        if(index < stack.size()){
            index++;
        }
        return stack.get(index);
    }

    public ImageContainer previous(){
        if(index > 0){
            index--;
        }
        return stack.get(index);
    }

    public boolean hasNext(){
        return (index < stack.size()-1);
    }

    public boolean hasPrevious(){
        return (index >= 0);
    }

    public void selectImage(ImageContainer selected){
        index = stack.indexOf(selected);
    }
}
