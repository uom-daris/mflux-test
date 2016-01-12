package mflux.client.test;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;

public class AuthenticationTest {

    public static final String HOST = "localhost";
    public static final boolean USE_HTTP = true;
    public static final boolean ENCRYPT = true;
    public static final int PORT = 9443;

    public static void main(String[] args) throws Throwable {
        String domain = args[0];
        String user = args[1];
        String password = args[3];
        RemoteServer rs = new RemoteServer(HOST, PORT, USE_HTTP, ENCRYPT);
        ServerClient.Connection cxn = null;
        try {
            cxn = rs.open();
            cxn.connect(domain, user, password);
            System.out.println(
                    "You've logged in. Your session id is " + cxn.sessionId());
        } finally {
            if (cxn != null) {
                cxn.close();
            }
        }
    }

}
