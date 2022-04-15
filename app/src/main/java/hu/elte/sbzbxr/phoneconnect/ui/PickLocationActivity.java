package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;

public class PickLocationActivity extends Activity {
    private static final int REQUEST_USER_CHOOSE_FOLDER = 3;
    public static final String CHOOSE_FOLDER = "request the user to choose a folder";
    public static final String FILENAME_TO_CREATE = "filename";
    public static final String FOLDERNAME_TO_CREATE = "foldername";
    public static final String URI_OF_FILE = "uri";
    private String tmpFilename;
    private String tmpFolderName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        tmpFilename = intent.getStringExtra(FILENAME_TO_CREATE);
        tmpFolderName = intent.getStringExtra(FOLDERNAME_TO_CREATE);
        letUserToChooseAFolder();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            Uri treeUri = resultData.getData();
            sendUri(treeUri);
        }
        finish();
    }

    private void letUserToChooseAFolder(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_USER_CHOOSE_FOLDER);
    }

    private void sendUri(Uri uri){
        Intent intent = new Intent(getApplicationContext(), ServiceController.class);
        intent.setAction(CHOOSE_FOLDER);
        intent.putExtra(URI_OF_FILE,uri);
        intent.putExtra(FILENAME_TO_CREATE,tmpFilename);
        intent.putExtra(FOLDERNAME_TO_CREATE,tmpFolderName);
        startService(intent);
    }
}
