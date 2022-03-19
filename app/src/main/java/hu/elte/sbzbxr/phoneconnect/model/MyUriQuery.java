package hu.elte.sbzbxr.phoneconnect.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyUriQuery {
    //Based on: https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content
    public static MyFileDescriptor querySingleFile(Uri uri, Context context){
        MyFileDescriptor ret;
        try(Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);){
            assert returnCursor != null;
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            int size = returnCursor.getInt(sizeIndex);
            ret = new MyFileDescriptor(name, size, uri);
        }
        return ret;
    }

    public static List<MyFileDescriptor> queryDirectory(ContentResolver contentResolver,
                                                        Uri collection, String idColumnIdentifier){
        List<MyFileDescriptor> descriptors = new ArrayList<MyFileDescriptor>();
        //Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        final String[] projection = new String[] {
                idColumnIdentifier,
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
        };

        try (Cursor cursor = contentResolver.query(
                collection,
                projection,
                null,null,null
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(idColumnIdentifier);
            int nameColumn = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
            int sizeColumn = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int size = cursor.getInt(sizeColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        collection, id);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                descriptors.add(new MyFileDescriptor(name, size, contentUri));
            }
        }
        System.err.println("Number of images to send: "+ descriptors.size());
        return descriptors;
    }



    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }
}
