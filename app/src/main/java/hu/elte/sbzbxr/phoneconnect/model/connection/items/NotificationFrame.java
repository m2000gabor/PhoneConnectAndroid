package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public class NotificationFrame extends NetworkFrame{
    public final String title;
    public final String text;
    public final String appName;

    public NotificationFrame(CharSequence title, CharSequence text, CharSequence appName) {
        this(String.valueOf(title),String.valueOf(text),String.valueOf(appName));
    }

    public NotificationFrame(String title, String text, String appName) {
        super(FrameType.NOTIFICATION, title);
        this.title = title;
        this.text = text;
        this.appName = appName;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(title).addField(text).addField(appName);
    }

    public static NotificationFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new NotificationFrame(deserializer.getString(),deserializer.getString(),deserializer.getString());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Notification:\nApp name: "+appName+"\nTitle: "+title
                +"\nText: "+text+"\n";
    }
}
