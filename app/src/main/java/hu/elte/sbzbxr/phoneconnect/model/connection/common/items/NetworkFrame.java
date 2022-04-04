package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public abstract class NetworkFrame {
    public final FrameType type;

    protected NetworkFrame(FrameType type) {
        this.type = type;
    }

    public boolean invalid(){return type==FrameType.INVALID;}

    public Serializer serialize(){
        Serializer serializer = new Serializer();
        return serializer.addField(type);
    }
}
