package hu.elte.sbzbxr.phoneconnect.model;

import android.graphics.Bitmap;

public class ScreenShot {
    private final String name;
    private final Bitmap bitmap;

    public ScreenShot(String name, Bitmap bitmap) {
        this.name = name;
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
