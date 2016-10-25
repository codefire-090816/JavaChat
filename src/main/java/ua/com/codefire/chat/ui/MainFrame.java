package ua.com.codefire.chat.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ua.com.codefire.chat.model.ChatContact;
import ua.com.codefire.chat.model.ChatMessage;
import ua.com.codefire.chat.net.ChatListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by human on 10/13/16.
 */
public class MainFrame extends JFrame implements ChatListener {

    private static final int SERVER_PORT = 21347;
    private static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final SimpleDateFormat SDF_WEEKTIME = new SimpleDateFormat("EEEE HH:mm:ss");
    private static final SimpleDateFormat SDF_CURRTIME = new SimpleDateFormat("HH:mm:ss");

    private JButton btnSend;
    private JPanel contentPanel;
    private JTextArea taHistory;
    private JTextArea taMessage;
    private JList<ContactModel> jlContacts;
    private JButton btnContactAdd;
    private JButton btnContactDel;
    private JLabel jlStatus;
    private JPanel panelStatus;
    private JMenuBar jMenuBar;
    // ------------------------------
    private List<ChatContact> chatContactList;
    private ChatContact currentChatContact;

    public MainFrame(String title) throws HeadlessException {
        super(title);
        this.chatContactList = Collections.synchronizedList(new ArrayList<ChatContact>());


        jMenuBar = new JMenuBar();
        JMenu jmenuFile = new JMenu("File");
        JMenuItem menuSettings = new JMenuItem("Settings");
        JMenuItem menuExit = new JMenuItem("Exit");

        jmenuFile.add(menuSettings);
        jmenuFile.add(menuExit);
        jMenuBar.add(jmenuFile);
        this.setJMenuBar(jMenuBar);

        btnSend.setEnabled(false);
        taMessage.setEnabled(false);

        setContentPane($$$getRootComponent$$$());
        pack();
        // Hook
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        taMessage.requestFocus();

        // LOAD CHAT CONTACT LIST
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.bin"))) {
            chatContactList = (List<ChatContact>) ois.readObject();

            System.out.println(chatContactList);

            updateContactList();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.bin"))) {
                    oos.writeObject(chatContactList);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        menuSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings settings = new Settings();
                settings.setVisible(true);
            }
        });


        btnContactAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = JOptionPane.showInputDialog(MainFrame.this, "Choose name:");

                if (nickname == null || nickname.isEmpty()) {
                    jlStatus.setText("Address must not be empty!");
                    return;
                }

                String address = JOptionPane.showInputDialog(MainFrame.this, "Enter IP:");

                if (address == null || address.isEmpty()) {
                    jlStatus.setText("Address must not be empty!");
                    return;
                }


                addNewContact(nickname, address);

            }
        });
        btnContactDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jlContacts.getSelectedIndex() >= 0) {
                    deleteSelectedContact();

                    btnSend.setEnabled(false);
                    taMessage.setEnabled(false);
                } else {
                    jlStatus.setText("Select contact in list before!");
                }
            }
        });

        jlContacts.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && jlContacts.getSelectedIndex() >= 0) {
                    loadSelectedContactHistory();
                }
            }
        });

        taMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();

                    sendMessage();
                } else {
                    currentChatContact.setTempMessage(taMessage.getText());
                }

            }
        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        menuExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);

            }
        });


    }

    private void loadSelectedContactHistory() {
        Iterator<ChatContact> iterator = chatContactList.iterator();

        ContactModel selectedContactModel = jlContacts.getSelectedValue();

        while (iterator.hasNext()) {
            ChatContact contact = iterator.next();
            if (contact.getAddress().equals(selectedContactModel.getAddress())
                    && contact.getNickname().equals(selectedContactModel.getNickname())) {
                currentChatContact = contact;
            }
        }

        if (currentChatContact != null) {
            taHistory.setText("");

            List<ChatMessage> chatMessages = currentChatContact.getChatMessages();
            System.out.println(chatMessages);

            for (ChatMessage msg : chatMessages) {
                if (msg != null) {
                    appendHistory(msg.isIncome() ? currentChatContact.getNickname() : "I'm", msg.getMessage(), msg.getTimestamp());
                }
            }

            taMessage.setText(currentChatContact.getTempMessage());

            btnSend.setEnabled(true);
            taMessage.setEnabled(true);
        } else {
            System.out.println("Contacts not found");
        }
    }

    private void deleteSelectedContact() {
        ContactModel selectedContact = jlContacts.getSelectedValue();

        Iterator<ChatContact> iterator = chatContactList.iterator();

        while (iterator.hasNext()) {
            ChatContact contact = iterator.next();
            if (contact.getAddress().equals(contact.getAddress()) && contact.getNickname().equals(contact.getNickname())) {
                iterator.remove();
            }
        }

        updateContactList();

        currentChatContact = null;
    }

    private void addNewContact(String nickname, String address) {
        chatContactList.add(new ChatContact(address, nickname));

        updateContactList();
    }

    private void updateContactList() {

        DefaultListModel<ContactModel> dlm = new DefaultListModel<>();

        for (ChatContact contact : chatContactList) {
            dlm.addElement(new ContactModel(contact.getAddress(), contact.getNickname()));
        }

        jlContacts.setModel(dlm);
    }

    private void sendMessage() {
        if (jlContacts.getSelectedIndex() < 0) {
            jlStatus.setText("Please select recipient.");
            return;
        }

        String message = taMessage.getText();
        String address = currentChatContact.getAddress();

        if (!message.isEmpty()) {
            try (Socket client = new Socket()) {
                client.setSoTimeout(3000);
                client.connect(new InetSocketAddress(address, SERVER_PORT));
                // Wrapping I/O
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                DataInputStream dis = new DataInputStream(client.getInputStream());

                // Send message to server
                dos.writeUTF(message);
                dos.flush();

                // Get response from server
                String response = dis.readUTF();

                // Validate response
                if ("OK".equals(response)) {
                    taMessage.setText("");

                    ChatMessage chatMessage = new ChatMessage(new Date(), message, address, false, true);

                    currentChatContact.getChatMessages().add(chatMessage);
                    currentChatContact.setTempMessage("");

                    appendHistory("I'm", chatMessage.getMessage(), chatMessage.getTimestamp());
                } else {
                    System.out.println("Error while sending message!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void receiveMessage(String address, String message) {
        ChatMessage chatMessage = new ChatMessage(new Date(), message, address, true, true);

        currentChatContact.getChatMessages().add(chatMessage);

        appendHistory(currentChatContact.getNickname(), chatMessage.getMessage(), chatMessage.getTimestamp());
    }

    private void appendHistory(String address, String message, Date when) {
        taHistory.append(String.format("%s [%s]:\n    %s\n", address, SDF_DATETIME.format(when), message));
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(240);
        splitPane1.setOrientation(0);
        contentPanel.add(splitPane1, new GridConstraints(0, 1, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(640, 320), null, 0, false));
        taHistory = new JTextArea();
        taHistory.setEditable(false);
        taHistory.setLineWrap(true);
        splitPane1.setLeftComponent(taHistory);
        taMessage = new JTextArea();
        taMessage.setLineWrap(true);
        splitPane1.setRightComponent(taMessage);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel1, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        btnSend = new JButton();
        btnSend.setText("SEND");
        panel1.add(btnSend, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel2, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jlContacts = new JList();
        panel2.add(jlContacts, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnContactAdd = new JButton();
        btnContactAdd.setText("+");
        panel3.add(btnContactAdd, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnContactDel = new JButton();
        btnContactDel.setText("-");
        panel3.add(btnContactDel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelStatus = new JPanel();
        panelStatus.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelStatus.setBackground(new Color(-6906979));
        contentPanel.add(panelStatus, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelStatus.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null));
        jlStatus = new JLabel();
        jlStatus.setText(" ");
        panelStatus.add(jlStatus, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
