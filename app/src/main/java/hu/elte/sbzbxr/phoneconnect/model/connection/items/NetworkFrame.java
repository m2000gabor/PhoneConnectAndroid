package hu.elte.sbzbxr.phoneconnect.model.connection.items;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public abstract class NetworkFrame {
    public final FrameType type;
    public final String name;

    protected NetworkFrame(FrameType type, String name) {
        this.type = type;
        this.name = name;
    }

    public boolean invalid(){return type==FrameType.INVALID;}

    public Serializer serialize(){
        Serializer serializer = new Serializer();
        return serializer.addField(type).addField(name);
    }
}
