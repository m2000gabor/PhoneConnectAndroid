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

    public NotificationFrame(NetworkFrame networkFrame, InputStream inputStream) {
        super(networkFrame);
        Optional<String> title = Optional.empty();
        Optional<String> text = Optional.empty();
        Optional<String> appName = Optional.empty();
        try{
            title = Optional.of(NetworkFrameCreator.readNextStringField(inputStream));
            text = Optional.of(NetworkFrameCreator.readNextStringField(inputStream));
            appName = Optional.of(NetworkFrameCreator.readNextStringField(inputStream));
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
        this.title = title.orElse("");
        this.text = text.orElse("");
        this.appName = appName.orElse("");
    }

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
    public InputStream getData(){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(getDataAsByteArray());
            outputStream.write(title.getBytes().length);
            outputStream.write(title.getBytes());
            outputStream.write(text.getBytes().length);
            outputStream.write(text.getBytes());
            outputStream.write(appName.getBytes().length);
            outputStream.write(appName.getBytes());
            return new ByteArrayInputStream(outputStream.toByteArray());
        }catch (IOException ioException){
            return super.getData();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Notification:\nApp name: "+appName+"\nTitle: "+title
                +"\nText: "+text+"\n";
    }
}
