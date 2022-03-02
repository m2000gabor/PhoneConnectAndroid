package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;

public class PickLocationActivity extends Activity {
    private static final int REQUEST_FILE_CREATION = 1;
    public static final String FILENAME_TO_CREATE = "filename";
    public static final String URI_OF_FILE = "uri";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        String filename = intent.getStringExtra(FILENAME_TO_CREATE);
        chooseSaveLocation(filename);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_FILE_CREATION){
            if(resultCode==RESULT_OK){
                if (data != null) {
                    Uri uri = data.getData();
                    sendUri(uri);
                }else{
                    Log.e(getLocalClassName(), "Data is null");
                }
            }else{
                Log.i(getLocalClassName(), "User cancelled");
                Toast.makeText(this, "User cancelled", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }


    private String tmpFilename;
    private void chooseSaveLocation(String filename){
        tmpFilename = filename;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        startActivityForResult(intent, REQUEST_FILE_CREATION);
    }

    private void sendUri(Uri uri){
        Intent intent = new Intent(getApplicationContext(), ConnectionManager.class);
        intent.putExtra(URI_OF_FILE,uri);
        intent.putExtra(FILENAME_TO_CREATE,tmpFilename);
        startService(intent);
    }
}
