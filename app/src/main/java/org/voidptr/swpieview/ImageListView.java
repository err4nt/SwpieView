package org.voidptr.swpieview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;

public class ImageListView extends AppCompatActivity {
    private ImageStack stack;

    private static final int DIRECTORY_CODE = 404;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list_view);

        stack = new ImageStack(this);

        ((GridView)findViewById(R.id.imageListGridView)).setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stack.setIndex(position);
                Intent viewIntent = new Intent(ImageListView.this,
                        ImageViewActivity.class);
                viewIntent.putExtra("stack", stack.toBundle());
                startActivity(viewIntent);
            }
        });

        if(getIntent().getType() == null){
            Intent odIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(odIntent, DIRECTORY_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DIRECTORY_CODE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();

            ContentResolver contentResolver = getContentResolver();
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));

            Cursor childCursor = contentResolver.query(childrenUri,
                    new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_MIME_TYPE},
                    null,
                    null,
                    null);

            try
            {
                ArrayList<ImageContainer> images = new ArrayList<>();
                assert childCursor != null;
                while(childCursor.moveToNext()){
                    if (childCursor.getString(1).startsWith("image")) {
                        ImageContainer container = new ImageContainer();
                        container.setUri(DocumentsContract
                                .buildChildDocumentsUriUsingTree(uri,
                                        childCursor.getString(0)));
                        container.setMimeType(childCursor.getString(1));
                        images.add(container);
                    }
                }
                Collections.sort(images);
                stack.setStack(images);
                ((GridView)findViewById(R.id.imageListGridView)).setAdapter(stack);
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
}
