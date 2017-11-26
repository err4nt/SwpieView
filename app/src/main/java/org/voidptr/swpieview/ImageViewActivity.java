package org.voidptr.swpieview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.felipecsl.gifimageview.library.GifImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ImageViewActivity extends AppCompatActivity{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    private static final int FILE_CODE = 403;
    private static final int DIRECTORY_CODE = 404;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private final Handler mHideHandler = new Handler();
    private GifImageView mContentView;
    private GestureDetector gestureDetector;
    private ImageStack stack;
    private Timer slideshowTimer;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation barQ

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private class CustomGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if(stack.hasNext()) {
                        setImage(stack.next());
                    }
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if(stack.hasPrevious()) {
                        setImage(stack.previous());
                    }
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            toggle();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_view);

        Intent intent = getIntent();

        stack = new ImageStack();

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (GifImageView)findViewById(R.id.fullscreen_image);

        CustomGestureDetector cgdt = new CustomGestureDetector();
        gestureDetector = new GestureDetector(this, cgdt);
        gestureDetector.setOnDoubleTapListener(cgdt);

        Button startStopButton = (Button)findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAnimating();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.start_stop_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.slideshow_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(slideshowTimer == null) {
                    slideshowTimer = new Timer();
                    ((Button) findViewById(R.id.slideshow_button)).setText(R.string.start_slideshow);
                    slideshowTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            setImage(stack.next());
                        }
                    }, 20000);
                }else{
                    ((Button) findViewById(R.id.slideshow_button)).setText(R.string.stop_slideshow);
                    slideshowTimer.cancel();
                    slideshowTimer = null;
                }

                return true;
            }
        });

        if (intent.getType() != null) {
            if(intent.getType().startsWith("image/")){
                buildFileList(intent);
            }
        }else{
            //Not opened by intent, so show the file selector
            Intent odIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            odIntent.setType("image/*");
            odIntent.addCategory(Intent.CATEGORY_OPENABLE);
            //odIntent.addCategory(Intent.CATEGORY_OPENABLE);
            //odIntent.setType("image/*");

            startActivityForResult(odIntent, DIRECTORY_CODE);
        }
    }

    protected void toggleAnimating()
    {
        if(mContentView.isAnimating())
        {
            stopAnimating();
        }
        else
        {
            startAnimating();
        }
    }

    protected void startAnimating()
    {
        if(!mContentView.isAnimating()) {
            mContentView.startAnimation();
            ((Button) findViewById(R.id.start_stop_button)).setText(R.string.pause_icon);
        }
    }

    protected void stopAnimating()
    {
        if(mContentView.isAnimating()) {
            mContentView.stopAnimation();
            ((Button) findViewById(R.id.start_stop_button)).setText(R.string.play_icon);
        }
    }

    protected void buildFileList(Intent data)
    {
        ContentResolver contentResolver = getContentResolver();
        Cursor childCursor = contentResolver.query(data.getData(), new String[]{
                        Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE}, null, null, null);
        assert childCursor != null;

        childCursor.moveToFirst();

        ImageContainer container = new ImageContainer();
        container.setUri(data.getData());
        try {
            container.setMimeType(childCursor.getString(1));
        }catch (Exception e) {
            //DJ: This fixes the issue with Conversations, but I'm not sure
            //    its the best long term solution.
            container.setMimeType(data.getType());
        }


        setImage(container);

        closeQuietly(childCursor);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == FILE_CODE && resultCode == Activity.RESULT_OK){
            setImage(new ImageContainer(data.getData()));
        }else if(requestCode == DIRECTORY_CODE && resultCode == Activity.RESULT_OK){
            Uri selectedUri = data.getData();

            //DJ: This is... gross. This gets the URI of the selected file, changes "document" to
            // "tree" and removes the file from the path to get the parent directory
            Uri.Builder builder = selectedUri.buildUpon();
            List<String> targetPathParts = selectedUri.getPathSegments();
            String targetPath = targetPathParts.get(1);
            targetPath = targetPath.substring(0, targetPath.lastIndexOf("/"));
            builder.path("tree");
            builder.appendPath(targetPath);
            Uri uri = builder.build();

            ContentResolver contentResolver = getContentResolver();
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));

            Cursor childCursor = contentResolver.query(childrenUri, new String[]{
                    Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE}, null, null, null);

            try
            {
                ArrayList<ImageContainer> images = new ArrayList<>();
                assert childCursor != null;
                while(childCursor.moveToNext()){
                    if (childCursor.getString(1).startsWith("image")) {
                        ImageContainer container = new ImageContainer();
                        container.setUri(DocumentsContract.buildChildDocumentsUriUsingTree(uri, childCursor.getString(0)));
                        container.setMimeType(childCursor.getString(1));
                        images.add(container);
                    }
                }
                stack.setStack(images);

                //This is... not great either, we need to show the image the user selected
                // so we bang apart the paths and compare them since the URIs are different
                List<String> searchParts = selectedUri.getPathSegments();
                for (ImageContainer image : images) {
                    List<String> pathParts = image.getUri().getPathSegments();
                    if (searchParts.get(1).equals(pathParts.get(3))) {
                        stack.selectImage(image);
                        setImage(image);
                    }
                }

            }finally {
                closeQuietly(childCursor);
            }
        }else if(requestCode == DIRECTORY_CODE){
            finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    private void setImage(ImageContainer image){
        stopAnimating();

        if((image != null && image.getMimeType().endsWith("gif"))){
            (findViewById(R.id.start_stop_button)).setEnabled(true);
            try {
                InputStream iStream = getContentResolver().openInputStream(image.getUri());
                mContentView.setBytes(getBytes(iStream));
                startAnimating();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            (findViewById(R.id.start_stop_button)).setEnabled(false);
            assert image != null;
            mContentView.setImageURI(image.getUri());
        }
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
