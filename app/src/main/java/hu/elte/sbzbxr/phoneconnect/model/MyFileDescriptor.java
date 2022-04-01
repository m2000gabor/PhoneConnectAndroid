package hu.elte.sbzbxr.phoneconnect.model;

import android.net.Uri;

public final class MyFileDescriptor {
    public final String filename;
    public final long size;
    public final Uri uri;

    public MyFileDescriptor(String filename, Long size, Uri uri) {
        this.filename = filename;
        this.size = size;
        this.uri = uri;
    }

}
