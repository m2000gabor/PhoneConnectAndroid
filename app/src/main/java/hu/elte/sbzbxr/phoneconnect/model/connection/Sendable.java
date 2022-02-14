package hu.elte.sbzbxr.phoneconnect.model.connection;

/**
 * Marks classes that are designed to be sendable over the network
 */
public interface Sendable {
    /**
     * @return a frame, including all data that needs to be sent over network
     */
    MyNetworkProtocolFrame toFrame();

    /**
     * @return a user readable typeName. i.e.: "Notification" or "file"
     */
    String getTypeName();
}
