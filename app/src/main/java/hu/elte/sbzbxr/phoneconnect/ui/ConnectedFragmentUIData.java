package hu.elte.sbzbxr.phoneconnect.ui;

public class ConnectedFragmentUIData {
    private final String ip;
    private final String port;
    private final boolean isStreaming;
    private final boolean isNotification;
    private final boolean isDemo;
    private final boolean isLimited;


    public ConnectedFragmentUIData(String ip, String port, boolean isStreaming, boolean isNotification, boolean isDemo, boolean isLimited) {
        this.ip = ip;
        this.port = port;
        this.isStreaming = isStreaming;
        this.isNotification = isNotification;
        this.isDemo = isDemo;
        this.isLimited = isLimited;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public boolean isNotificationForwarded() {
        return isNotification;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public boolean isLimited() {
        return isLimited;
    }
}
