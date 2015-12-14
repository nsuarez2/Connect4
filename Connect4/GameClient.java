package Connect4;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.util.HashSet;

/**
 * Created by Nico on 12/1/15.
 */
public class GameClient extends JFrame {

    private JPanel dropButtonsPanel;
    private JPanel boardGridPanel;
    private JPanel boardGridPanelHolder;
    private JPanel mainGameHolder;
    private JPanel messageBoard;
    private JLabel messageLabel;
    private JPanel chatPanel;
    private JButton sendButton;
    private JTextField enterMessage;
    private JTextArea chatBox;
    private JLabel chatLabel;
    private JButton[] dropButtons;
    private JLabel[][] squares;
    private Color player;
    private String myName;
    private String enemy;
    private String thisColor;
    private HashSet<Integer> clickableButtons;

    private final ImageIcon redSquare;
    private final ImageIcon yellowSquare;
    private final ImageIcon whiteSquare;

    private final Dimension buttonDimension;

    private int numCols;
    private int numRows;

    private Board board;

    private ClientServerSocket clientServerSocket;
    private ClientServerSocket chatSocket;


    public GameClient(int inNumRows, int inNumCols,
                      ClientServerSocket inCSS) throws Exception {

        super("Connect 4 - waiting");

        clientServerSocket = inCSS;

        numCols = inNumCols;
        numRows = inNumRows;
        board = new Board(numRows, numCols);

        buttonDimension = new Dimension(95, 35);

        chatPanel = new JPanel(new BorderLayout());
        dropButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        boardGridPanel = new JPanel(new GridLayout(inNumRows, numCols, 0, 0));
        boardGridPanelHolder = new JPanel(); // keeps the board put together
        mainGameHolder = new JPanel(new BorderLayout()); // same as above
        messageBoard = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messageLabel = new JLabel("Waiting for client, please wait");
        messageBoard.add(messageLabel);

        final int SQUARE_SIZE = 100;

        redSquare = getScaled("/images/Red.jpg", SQUARE_SIZE);
        yellowSquare = getScaled("/images/Yellow.jpg", SQUARE_SIZE);
        whiteSquare = getScaled("/images/White.jpg", SQUARE_SIZE);

        setLayout(new BorderLayout());

        paintSquares();
        initDropButtons();
        mainGameHolder.add(messageBoard, BorderLayout.NORTH);
        mainGameHolder.add(dropButtonsPanel);
        mainGameHolder.add(boardGridPanelHolder, BorderLayout.SOUTH);

        JPanel bottomChatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
                0, 0));

        enterMessage = new JTextField(15);
        sendButton = new JButton("Send");

        // Allows messages to be entered without clicking in the field
        enterMessage.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                //Do nothing
            }
            public void focusLost(FocusEvent e) {
                enterMessage.requestFocusInWindow();
            }
        });

        // Allows messages to send by hitting enter
        JRootPane rootPane = getRootPane();
        rootPane.setDefaultButton(sendButton);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = enterMessage.getText();
                if (message.length() >= 1) {
                    chatBox.append(myName + ": " + message + "\n");
                    enterMessage.setText("");
                    chatSocket.sendString(message);
                }
            }
        });

        bottomChatPanel.add(enterMessage, BorderLayout.WEST);
        bottomChatPanel.add(sendButton);

        JPanel chatLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        chatLabel = new JLabel("Chatting as " + myName);
        chatLabelPanel.add(chatLabel);

        chatBox = new JTextArea();
        chatBox.setLineWrap(true);
        chatBox.setEditable(false);

        chatPanel.add(chatLabelPanel, BorderLayout.NORTH);
        chatPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);
        chatPanel.add(bottomChatPanel, BorderLayout.SOUTH);

        add(mainGameHolder, BorderLayout.WEST);
        add(chatPanel, BorderLayout.EAST);

        pack();
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // Processes and completes move
    private void doMove(int col, String color, boolean isMyMove) {


        // Not that "addPiece" will check for a winner and return the color
        // of them if there is one
        Color result = board.addPiece(col, (color.equals("Red"))?
                Color.RED: Color.YELLOW);

        updateIcon(col, color);

        String messageToUpdate = "Nice move! Please wait";

        if (result != Color.BAD_COLOR) {

            if (isMyMove) {
                clientServerSocket.sendString("MOVE "+ col + " " + color);
            }


            // a result of OPEN_COLOR, means nothing is to be done
            if (result != Color.OPEN_COLOR) {

                // the column inserted into is full now
                if (result == Color.FULL_COLOR) {

                    // don't let it be clicked anymore
                    disableButton(col);

                    // Does this mean we hit a tie?
                    if (board.isEmpty()) {
                        showGameResult(result, isMyMove);
                        messageToUpdate = "";
                    }
                }
                else {
                    // Red or yellow has now won
                    showGameResult(result, isMyMove);
                    messageToUpdate = "";
                }
            }
        }
        else {
            System.out.println("AddPiece returned BAD_COLOR");
            System.exit(1);
        }

        if (isMyMove) waitYourTurn(messageToUpdate);
    }

    // Waits for the other person to do something
    // changes message board if string passed in is not empty
    public void waitYourTurn(String waitingMessage) {
        if (!waitingMessage.equals("")) updateMessageBoard(waitingMessage);
        disableAllButtons();
        new LogicWorker().execute();
    }

    // reads in the actions from the socket, and decides what to do with them
    class LogicWorker extends SwingWorker< Integer, Integer > {

        private String action;
        public Integer doInBackground() {
            action = clientServerSocket.recvString();
            return 0;
        }
        public void done() {
            if (action.startsWith("MOVE")) {

                // This means the other player moved, we need to process it

                int col = Integer.parseInt(action.substring(5,6));
                String color = action.substring(7);
                enableAllButtons();
                updateMessageBoard(enemy + " played column " + (col+1) +
                        " - Your turn");
                doMove(col, color, false);
            }
            else if (action.startsWith("AGAIN")) {

                // This means the other player wants to play again

                Object [] options = { "Yes, this game rocks!",
                        "No thanks, I'm lame" };
                int result = JOptionPane.showOptionDialog(getContentPane(),
                        enemy + " wants to play again, do you?",
                        "You probably should",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                // Does this player want to accept?
                if (result == 0) {

                    // Notify other player that we do
                    clientServerSocket.sendString("ACCEPT");

                    // Reset board/gui
                    startAgain();
                    waitYourTurn("Loser plays first - please wait");
                }
                else {

                    // Notify other player that we don't
                    clientServerSocket.sendString("QUIT");
                }
            }
            else if (action.startsWith("ACCEPT")) {

                // The other player has accepted a rematch, reset board/gui

                JOptionPane.showMessageDialog(getContentPane(),
                        enemy + " accepted!", "Yay!",
                        JOptionPane.INFORMATION_MESSAGE);

                startAgain();
                updateMessageBoard("You lost - your move");
            }
            else if (action.startsWith("QUIT")) {

                // The other player does not want to play again

                JOptionPane.showMessageDialog(getContentPane(),
                        enemy + " does not want to play again", "sad :(",
                        JOptionPane.INFORMATION_MESSAGE);

                updateMessageBoard(enemy + " sucks and doesn't want to play" +
                        " again");
            }
            else if (action.startsWith("SET_HOST")) {

                // The other host is telling us (the client) their name
                // The host goes first initially so we must wait

                setEnemy(action.substring(9));
                waitYourTurn("Red goes first, please wait");
            }
        }
    }

    class ChatWorker extends SwingWorker<Integer, Integer> {

        String message = "";
        public Integer doInBackground() {
            message = chatSocket.recvString();
            return 0;
        }
        public void done() {
            sendEnemyMessage(message);
            new ChatWorker().execute();
        }
    }

    public void sendEnemyMessage(String message) {
        if (message.length() >= 1) {
            chatBox.append(enemy + ": " + message + "\n");
        }
    }

    // Updates the top position of the specified column to the specified color
    public void updateIcon(int col, String color) {
        squares[board.topOf(col)][col].setIcon((color.equals("Red"))?
                redSquare: yellowSquare);
    }


    // Make the board grid, all initialized with white circles, signifying empty
    public void paintSquares() {
        boardGridPanelHolder.removeAll();
        boardGridPanel.removeAll();
        squares = new JLabel[numRows][numCols];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                squares[r][c] = new JLabel();
                squares[r][c].setHorizontalAlignment(SwingConstants.CENTER);
                squares[r][c].setIcon(whiteSquare);
                boardGridPanel.add(squares[r][c]);
            }
        }
        boardGridPanelHolder.add(boardGridPanel);
    }

    // Make all of the drop buttons
    public void initDropButtons() {
        dropButtonsPanel.removeAll();
        dropButtons = new JButton[numCols];
        clickableButtons = new HashSet<>();
        for (int i = 0; i < numCols; i++) {
            dropButtons[i] = new JButton(" ↓ ");
            dropButtons[i].setPreferredSize(buttonDimension);
            final int col = i;
            clickableButtons.add(col);
            dropButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    doMove(col, thisColor, true);
                }
            });
            dropButtonsPanel.add(dropButtons[i]);
        }
    }

    // This resets the GUI to a "fresh game" look when staring a rematch
    // Also creates a new board
    private void startAgain() {

        mainGameHolder.removeAll();

        board = new Board(numRows, numCols);

        paintSquares();
        initDropButtons();
        mainGameHolder.add(messageBoard, BorderLayout.NORTH);
        mainGameHolder.add(dropButtonsPanel);
        mainGameHolder.add(boardGridPanelHolder, BorderLayout.SOUTH);

        mainGameHolder.validate();
        pack();
        validate();
    }

    // Changes the message displayed on the bottom bar to the specified string
    public void updateMessageBoard(String toChange) {
        messageLabel.setText(toChange);
    }

    // Called when a winner is found or when a tie occurs,
    // this shows the players the result and then prompts them to init a rematch
    private void showGameResult(Color winner, boolean isMyMove) {
        String messageText, windowText;
        switch (winner) {
            case RED:
                messageText = "Red won";
                windowText = "RED WINS!!! :)";
                break;
            case YELLOW:
                messageText = "Yellow won";
                windowText = "YELLOW WINS!!! :)";
                break;
            case FULL_COLOR:
                messageText = "Tie game";
                windowText = "We have a tie";
                break;
            default:
                messageText = "I don't know where I am";
                windowText = "This message shouldn't be here, tell Nico";
        }

        disableAllButtons();

        // The losing player is the one to make the first call about possibly
        // starting a rematch
        if (isMyMove) {
            JOptionPane.showMessageDialog(getContentPane(), windowText + "\n" +
                    enemy + " will now decide if they want to play again",
                    "Game results", JOptionPane.INFORMATION_MESSAGE);

            updateMessageBoard(messageText + " - please wait while " + enemy +
                    " decides to play again");
        }
        else {
            Object [] options = { "Yes, this game rocks!",
                                  "No thanks, I'm lame" };
            int result = JOptionPane.showOptionDialog(getContentPane(),
                    windowText + "\nWould you like to play again?",
                    "Game results",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            // Does the loser want to play again?
            if (result == 0) {
                // Notify the winner yes
                clientServerSocket.sendString("AGAIN");
                updateMessageBoard("Letting " + enemy + " decide now - " +
                        "please wait");
                waitYourTurn("");
            }
            else {
                // Notify the winner no
                clientServerSocket.sendString("QUIT");
                updateMessageBoard("Sad to see you go :(");
            }
        }
    }

    // Makes specified button not clickable anymore, because the column is full
    private void disableButton(int col) {
        dropButtons[col].setText("FULL");
        dropButtons[col].setEnabled(false);
        clickableButtons.remove(col);
    }

    // Makes all of the drop buttons not clickable
    public void disableAllButtons() {
        for (JButton it : dropButtons) {
            it.setEnabled(false);
        }
    }

    // Makes the drop buttons clickable, but only the ones who's columns aren't
    // already full
    public void enableAllButtons() {
        for (int i: clickableButtons) {
            dropButtons[i].setEnabled(true);
        }
    }

    // Sets the player of this GUI
    public void setPlayer(Color inColor) {
        player = inColor;
        thisColor = (player == Color.RED)? "Red": "Yellow";
        setTitle("Connect 4 - " + thisColor);
    }

    public void setChatSocket(ClientServerSocket inCSS) {
        chatSocket = inCSS;
    }

    public void setMyName(String name) {
        myName = name;
        chatLabel.setText("Chatting as " + myName);
        pack();
    }
    public String getMyName() {
        return myName;
    }
    public void setEnemy(String name) {
        enemy = name;
    }
    public String getEnemy() {
        return enemy;
    }

    public void startChat() {
        enterMessage.requestFocusInWindow();
        new ChatWorker().execute();
    }

    // Returns a scaled version of the specified image, to the specified size
    private ImageIcon getScaled(String urlPath, int size) {
        URL url = getClass().getResource(urlPath);
        ImageIcon temp = new ImageIcon(url);
        Image image = temp.getImage();
        Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
