package Connect4;


import javax.swing.JOptionPane;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import static java.lang.System.out;

public class ClientServerSocket {

    private String ipAddr;
    private int portNum;
    private Socket socket;
    private DataOutputStream outData;
    private DataInputStream inData;


    public ClientServerSocket(int inPortNum) {
        ipAddr = null;
        portNum = inPortNum;
        inData = null;
        outData = null;
        socket = null;
    }

    public void setIpAddr(String inIpAddr) {
        ipAddr = inIpAddr;
    }
    public String getIpAddr() { return ipAddr; }
    public int getPortNum() { return portNum; }

    public void startClient() {
        try {
            socket = new Socket(ipAddr, portNum);
            outData = new DataOutputStream(socket.getOutputStream());
            inData = new DataInputStream(socket.getInputStream());
        }
        catch (IOException ioe) {

            Object [] options = { "Silly me. Will do!" };

            JOptionPane.showOptionDialog(null,
                    "Unable to connect\nPlease make sure the host is running " +
                            "first and then try again",
                    "Oops :(",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);


            System.exit(10);
        }
    }

    public void startServer() {
        ServerSocket serverSock;
        try {
            serverSock = new ServerSocket(portNum);
            out.println("Waiting for client to connect...");
            socket = serverSock.accept();
            outData = new DataOutputStream(socket.getOutputStream());
            inData = new DataInputStream(socket.getInputStream());
            out.println("Client connection accepted");
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
                    "There seems to be a problem starting the game, see console"
                    , "Oops :(",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(11);
        }
    }

    public boolean sendString(String strToSend) {
        boolean success = false;
        try {
            outData.writeBytes(strToSend);
            outData.writeByte(0); //send 0 to signal the end of the string
            success = true;
        }
        catch (IOException e) {

            Object [] options = { "It's okay Nico, you're still the best!" };

            JOptionPane.showOptionDialog(null,
                    "It seems as if you have disconnected, or the enemy has " +
                            "quit. \nSorry for the inconvenience",
                    "Oops :(",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);

            System.exit(12);
        }
        return (success);
    }

    public String recvString() {
        Vector< Byte > byteVec = new Vector< Byte >();
        byte [] byteAry;
        byte recByte;
        String receivedString = "";
        try {
            recByte = inData.readByte();

            while (recByte != 0) {
                byteVec.add(recByte);
                recByte = inData.readByte();
            }
            byteAry = new byte[byteVec.size()];

            for (int ind = 0; ind < byteVec.size(); ind++) {
                byteAry[ind] = byteVec.elementAt(ind).byteValue();
            }
            receivedString = new String(byteAry);
        }
        catch (IOException ioe) {

            Object [] options = { "It's okay Nico, you're still the best!" };

            JOptionPane.showOptionDialog(null,
                    "It seems as if you have disconnected, or the enemy has " +
                            "quit. \nSorry for the inconvenience",
                    "Oops :(",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);


            System.exit(13);
        }
        return (receivedString);
    }

}