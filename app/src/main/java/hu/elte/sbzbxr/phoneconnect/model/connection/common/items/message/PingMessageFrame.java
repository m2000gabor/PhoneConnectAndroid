package hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message;

import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Serializer;

//version 2.2
public class PingMessageFrame extends MessageFrame{
    public final String message;
    private final long created;
    private long sentRequest;
    private long requestArrived;
    private long answerArrived;

    public PingMessageFrame(String message) {
        this(message,System.currentTimeMillis(),0,0,0);
    }

    public PingMessageFrame(String message, long created, long sentRequest, long requestArrived, long answerArrived) {
        super(MessageType.PING);
        this.message = message;
        this.created = created;
        this.sentRequest = sentRequest;
        this.requestArrived = requestArrived;
        this.answerArrived = answerArrived;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().
                addField(message).
                addField(created).addField(sentRequest).
                addField(requestArrived).
                addField(answerArrived);
    }

    public static PingMessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer d = new Deserializer(inputStream);
        return new PingMessageFrame(d.getString(),d.getLong(),d.getLong(),d.getLong(),d.getLong());
    }

    public void requestArrived(){
        requestArrived =System.currentTimeMillis();
    }

    public void rightBeforeRequest(){
        sentRequest=System.currentTimeMillis();
    }

    public void answerArrived(){
        answerArrived=System.currentTimeMillis();
    }

    public int calculateLatency(){
        return (int) (answerArrived-sentRequest);
    }

    @Override
    public String toString() {
        return "PingMessageFrame{" +
                "message='" + message + '\'' +
                ", created=" + created +
                ", sentRequest=" + sentRequest +
                ", requestArrived=" + requestArrived +
                ", answerArrived=" + answerArrived +
                '}';
    }
}
