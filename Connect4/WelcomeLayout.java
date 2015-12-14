package Connect4;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Nico on 12/10/15.
 */
public class WelcomeLayout extends JPanel {

    private JLabel welcomeMessage;
    private JButton hostButton;
    private JButton joinButton;

    private String hostIP;
    private ClientServerSocket clientServerSocket;
    private ClientServerSocket chat;
    private GameClient game;

    public WelcomeLayout(ClientServerSocket css, GameClient gl) {

        hostIP = "localhost";

        clientServerSocket = css;
        chat = new ClientServerSocket(clientServerSocket.getPortNum() + 1);
        game = gl;

        JPanel pan = new JPanel();
        JPanel panHolder = new JPanel();
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel hostPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        Dimension buttonDim = new Dimension(150, 75);

        welcomeMessage = new JLabel("I would like to...");
        messagePanel.add(welcomeMessage);

        hostButton = new JButton("Host a game");
        hostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InetAddress IP = null;
                try {
                    IP = InetAddress.getLocalHost();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }

                String name = JOptionPane.showInputDialog(getParent(),
                        "Please enter your name:", "Hosting game",
                        JOptionPane.PLAIN_MESSAGE);

                name = (name.equals(""))? "Someone who's too cool to enter a " +
                        "name": name;

                game.setMyName(name);
                game.setPlayer(Color.RED);
                game.updateMessageBoard("Waiting for enemy to connect - tell " +
                        "them your IP address is: " + IP.getHostAddress());
                game.disableAllButtons();
                new StartServerWorker().execute();
                setVisible(false);
            }
        });
        hostButton.setPreferredSize(buttonDim);
        hostPanel.add(hostButton);

        joinButton = new JButton("Join a game");
        joinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String name = JOptionPane.showInputDialog(getParent(),
                        "Please enter your name:", "Joining game",
                        JOptionPane.PLAIN_MESSAGE);

                name = (name.equals(""))? "Someone who's too cool to enter a " +
                        "name": name;
                game.setMyName(name);

                hostIP = JOptionPane.showInputDialog(getParent(),
                        "Welcome, " + name + "!\nPlease enter host's IP " +
                                "address:",
                        "Joining game",
                        JOptionPane.PLAIN_MESSAGE);

                if (hostIP.length() == 0) hostIP = "localhost";

                clientServerSocket.setIpAddr(hostIP);
                clientServerSocket.startClient();
                new ChatServerWorker().execute();
                clientServerSocket.sendString("SET_CLIENT " + name);
                game.setPlayer(Color.YELLOW);
                game.waitYourTurn("");
                setVisible(false);
            }
        });
        joinButton.setPreferredSize(buttonDim);
        joinPanel.add(joinButton);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
        pan.add(messagePanel);
        pan.add(hostPanel);
        pan.add(joinPanel);

        panHolder.add(pan);
        add(panHolder);
    }


    // A worker class to help start the server, which is blocking code
    class StartServerWorker extends SwingWorker<Integer,Integer> {

        String action = "";
        public Integer doInBackground() throws Exception {
            clientServerSocket.startServer();

            // Let the client know your name
            clientServerSocket.sendString("SET_HOST " + game.getMyName());

            // recv the client's name
            action = clientServerSocket.recvString();
            return 0;
        }
        public void done() {
            String clientName = "Enemy";
            if (action.startsWith("SET_CLIENT")) {
                clientName = action.substring(11);

            }
            game.setEnemy(clientName);

            action = clientServerSocket.recvString();

            if (action.startsWith("START_CHAT")) {
                String IP = action.substring(11);
                chat.setIpAddr(IP);
                chat.startClient();
            }

            game.setChatSocket(chat);
            game.startChat();
            game.updateMessageBoard(clientName + " connected - your turn");

            // Allow the host to start playing
            game.enableAllButtons();
        }
    }

    class ChatServerWorker extends SwingWorker<Integer,Integer> {

        protected Integer doInBackground() throws Exception {

            InetAddress IP = null;
            try {
                IP = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }

            clientServerSocket.sendString("START_CHAT " + IP.getHostAddress());
            chat.startServer();

            return 0;
        }
        protected void done() {

            game.setChatSocket(chat);
            game.startChat();
        }
    }

}
