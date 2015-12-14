package Connect4;

/**
 * Created by Nico on 12/12/15.
 */
public class Connect4 {

    public static void main(String[] args) throws Exception {

        ClientServerSocket clientServerSocket = new ClientServerSocket(2025);

        GameClient gameWindow  = new GameClient(6, 7, clientServerSocket);
        WelcomeLayout welcome = new WelcomeLayout(clientServerSocket, gameWindow);

        welcome.setPreferredSize(gameWindow.getSize());

        gameWindow.setGlassPane(welcome);

        welcome.setVisible(true);
        gameWindow.setVisible(true);
    }
}
