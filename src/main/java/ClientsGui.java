import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientsGui extends Thread {

    final JTextPane fieldForChats = new JTextPane();
    final JTextField fieldForEnterMessage = new JTextField();
    private String oldMsg = "";
    private Thread read;
    private String serverName;
    private int port;
    private String name;
    private BufferedReader input;
    private PrintWriter output;
    private Socket server;

    public void ClientsGui() {
        this.serverName = "localhost";
        this.port = 9999;
        this.name = "Please write you nickname!";

        String fontFamily = "Arial, sans-serif";
        Font font = new Font(fontFamily, Font.PLAIN, 15);

        final JFrame jFrame = new JFrame("Chat");
        jFrame.getContentPane().setLayout(null);
        jFrame.setSize(700, 500);
        jFrame.setResizable(false);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Field from chat
        fieldForChats.setBounds(25, 25, 635, 320);
        fieldForChats.setFont(font);
        fieldForChats.setMargin(new Insets(6, 6, 6, 6));
        fieldForChats.setEditable(false);
        JScrollPane jScrollPaneChatField = new JScrollPane(fieldForChats);
        jScrollPaneChatField.setBounds(25, 25, 635, 320);

        fieldForChats.setContentType("text/html");
        fieldForChats.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        // Field input message user
        fieldForEnterMessage.setBounds(0, 350, 400, 50);
        fieldForEnterMessage.setFont(font);
        fieldForEnterMessage.setMargin(new Insets(6, 6, 6, 6));
        final JScrollPane fieldForMessage = new JScrollPane(fieldForEnterMessage);
        fieldForMessage.setBounds(25, 350, 650, 50);

        // Send button
        final JButton sendButton = new JButton("Send");
        sendButton.setFont(font);
        sendButton.setBounds(575, 410, 100, 35);

        // Disconnect button
        final JButton disconnect = new JButton("Disconnect");
        disconnect.setFont(font);
        disconnect.setBounds(25, 410, 130, 35);

        fieldForEnterMessage.addKeyListener(new KeyAdapter() {
            // Send message on Enter
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }

                // Get last message typed
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    String currentMessage = fieldForEnterMessage.getText().trim();
                    fieldForEnterMessage.setText(oldMsg);
                    oldMsg = currentMessage;
                }
            }
        });

        // Click on send button
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendMessage();
            }
        });

        // Connection view
        final JTextField nameField = new JTextField(this.name);
        final JTextField portField = new JTextField(Integer.toString(this.port));
        final JTextField serverNameField = new JTextField(this.serverName);
        final JButton connectButton = new JButton("Connect");

        // Check if this field not empty
        nameField.getDocument().addDocumentListener(new TextListener(nameField, portField,
                serverNameField, connectButton));
        portField.getDocument().addDocumentListener(new TextListener(nameField, portField,
                serverNameField, connectButton));
        serverNameField.getDocument().addDocumentListener(new TextListener(nameField, portField,
                serverNameField, connectButton));

        // Location of modules
        connectButton.setFont(font);
        serverNameField.setBounds(25, 380, 135, 40);
        nameField.setBounds(375, 380, 135, 40);
        portField.setBounds(200, 380, 135, 40);
        connectButton.setBounds(575, 380, 100, 40);

        // Default chat background color
        fieldForChats.setBackground(Color.LIGHT_GRAY);

        // Adding elements
        jFrame.add(connectButton);
        jFrame.add(jScrollPaneChatField);
        jFrame.add(nameField);
        jFrame.add(portField);
        jFrame.add(serverNameField);
        jFrame.setVisible(true);

        // Information in the chat
        appendToPane(fieldForChats, "<h1>Welcome to my Java Chat</h1>"
                + "<ul>"
                + "<li>If you want to change your nickname, you need to enter the command \"/nick\","
                + " add a space and write a new nickname!</h4>"
                + "<li>Press Enter to send the message!</li>"
                + "<li>To get your last message, press UP on the keyboard!</li>"
                + "</ul><br>");

        // On connect
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    name = nameField.getText();
                    String port = portField.getText();
                    serverName = serverNameField.getText();
                    ClientsGui.this.port = Integer.parseInt(port);

                    appendToPane(fieldForChats, "<span>Connecting to " + serverName +
                            " on port " + ClientsGui.this.port + "...</span>");
                    server = new Socket(serverName, ClientsGui.this.port);

                    appendToPane(fieldForChats, "<span>Connected to " +
                            server.getRemoteSocketAddress() + "</span>");

                    input = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    output = new PrintWriter(server.getOutputStream(), true);

                    // Send nickname to server
                    output.println(name);

                    // Create new Read Thread
                    read = new Read();
                    read.start();
                    jFrame.remove(nameField);
                    jFrame.remove(portField);
                    jFrame.remove(serverNameField);
                    jFrame.remove(connectButton);
                    jFrame.add(sendButton);
                    jFrame.add(fieldForMessage);
                    jFrame.add(disconnect);
                    jFrame.revalidate();
                    jFrame.repaint();
                    fieldForChats.setBackground(Color.WHITE);
                } catch (Exception ex) {
                    appendToPane(fieldForChats, "<span>Could not connect to Server</span>");
                    JOptionPane.showMessageDialog(jFrame, ex.getMessage());
                }
            }

        });

        // Return to the start page, after disconnection
        disconnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                jFrame.add(nameField);
                jFrame.add(portField);
                jFrame.add(serverNameField);
                jFrame.add(connectButton);
                jFrame.remove(sendButton);
                jFrame.remove(fieldForMessage);
                jFrame.remove(disconnect);
                jFrame.revalidate();
                jFrame.repaint();
                read.interrupt();
                fieldForChats.setBackground(Color.LIGHT_GRAY);
                appendToPane(fieldForChats, "<span>Connection closed.</span>");
                output.close();
            }
        });

    }

    // Сheck if all field are not empty
    public class TextListener implements DocumentListener {
        JTextField textField1;
        JTextField textField2;
        JTextField textField3;
        JButton jButton;

        public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn) {
            this.textField1 = jtf1;
            this.textField2 = jtf2;
            this.textField3 = jtf3;
            this.jButton = jcbtn;
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
            if (textField1.getText().trim().equals("") ||
                    textField2.getText().trim().equals("") ||
                    textField3.getText().trim().equals("")) {
                jButton.setEnabled(false);
            } else {
                jButton.setEnabled(true);
            }
        }

        public void insertUpdate(DocumentEvent e) {
            if (textField1.getText().trim().equals("") ||
                    textField2.getText().trim().equals("") ||
                    textField3.getText().trim().equals("")) {
                jButton.setEnabled(false);
            } else {
                jButton.setEnabled(true);
            }
        }

    }

    // Sending messages
    public void sendMessage() {
        try {
            String message = fieldForEnterMessage.getText().trim();
            if (message.equals("")) {
                return;
            }
            this.oldMsg = message;
            output.println(message);
            fieldForEnterMessage.requestFocus();
            fieldForEnterMessage.setText(null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {
        ClientsGui client = new ClientsGui();
        client.ClientsGui();
    }

    // Read new incoming messages
    class Read extends Thread {
        public void run() {
            String message;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    message = input.readLine();
                    appendToPane(fieldForChats, message);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to parse incoming message");
                }
            }
        }
    }

    // Send html to pane
    private void appendToPane(JTextPane tp, String msg) {
        HTMLDocument doc = (HTMLDocument) tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
            tp.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

