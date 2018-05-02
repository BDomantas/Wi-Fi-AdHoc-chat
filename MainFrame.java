package Routing;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/** Main messaging GUI window */
public class MainFrame extends JFrame implements ActionListener, Observer {

  private final JButton send;
  private final JButton file;
  private JList userList;
  private String userName;
  private JTextPane messageLog;
  private JTextField inputTextBox;
  private DefaultListModel<String> userListContainer = new DefaultListModel<>();
  private Controller mainController;
  private Observable communicatorObservable;
  private Observable clientObservable;

  public MainFrame(Controller controller) {
    mainController = controller;
    userName = controller.getClientName();
    communicatorObservable = controller.getCommunicator();
    clientObservable = controller.getClient();
    communicatorObservable.addObserver(this);
    clientObservable.addObserver(this);
    Font f = new Font("MyFont", Font.ITALIC, 15);
    Font h = new Font("MyFont", Font.ITALIC, 17);
    setTitle("Chat box of group 20");
    getContentPane().setLayout(null);
    setSize(800, 600);
    setResizable(false);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    WindowListener exitListener =
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            int confirm =
                JOptionPane.showOptionDialog(
                    null,
                    "Are You Sure to Close This Application?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    null);
            if (confirm == 0) {
              // mainController.send(Message.messageType.CLOSE, );
              mainController.terminateClient();
              System.exit(0);
            }
          }
        };
    addWindowListener(exitListener);
    messageLog = new JTextPane();
    userList = new JList(userListContainer);
    inputTextBox = new JTextField();
    send = new JButton("Send");
    file = new JButton("Add file");

    JScrollPane messageLogScroller = new JScrollPane(messageLog);
    JScrollPane userListScroller = new JScrollPane(userList);
    JScrollPane inputTextBoxScroller = new JScrollPane(inputTextBox);

    messageLog.setCaretPosition(messageLog.getDocument().getLength());
    ((DefaultCaret) this.messageLog.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    JScrollBar verticalBar = messageLogScroller.getVerticalScrollBar();
    AdjustmentListener downScroller =
        new AdjustmentListener() {
          @Override
          public void adjustmentValueChanged(AdjustmentEvent e) {
            Adjustable adjustable = e.getAdjustable();
            adjustable.setValue(adjustable.getMaximum());
            verticalBar.removeAdjustmentListener(this);
          }
        };
    verticalBar.addAdjustmentListener(downScroller);

    messageLog.setFont(h);
    inputTextBox.setFont(f);
    send.setFont(f);
    file.setFont(f);
    userList.setFont(f);

    messageLog.setBounds(25, 25, 540, 370);
    userList.setBounds(570, 25, 200, 370);
    inputTextBox.setBounds(0, 400, 500, 50);

    inputTextBox.setColumns(20);

    messageLog.setMargin(new Insets(6, 6, 6, 6));
    // userList.setMargin(new Insets(6, 6, 6, 6));
    inputTextBox.setMargin(new Insets(6, 6, 6, 6));

    messageLogScroller.setBounds(25, 25, 540, 370);
    userListScroller.setBounds(570, 25, 200, 370);
    inputTextBoxScroller.setBounds(25, 400, 750, 50);
    send.setBounds(600, 460, 150, 75);
    file.setBounds(100, 480, 150, 35);

    send.setBackground(Color.lightGray);
    file.setBackground(Color.lightGray);

    messageLog.setEditable(false);
    inputTextBox.setLayout(new BorderLayout());
    inputTextBox.setCaretPosition(inputTextBox.getText().length());

    try {
      messageLog
          .getStyledDocument()
          .insertString(
              messageLog.getStyledDocument().getLength(),
              "Welcome to chat of group 20" + "\n\n",
              null);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    inputTextBox.addKeyListener(
        new KeyAdapter() {
          public void keyTyped(KeyEvent e) {
            if (inputTextBox.getText().length() > 128) e.consume();
          }
        });

    inputTextBox.addActionListener(
        ae -> {
          String input = inputTextBox.getText();
          try {
            DateFormat dateFormat = new SimpleDateFormat("E HH:mm:ss");
            Date date = new Date();
            if (input.length() > 0 && !userList.isSelectionEmpty()) {
              mainController.send(
                  Message.messageType.ONE, userList.getSelectedValue().toString(), input, true);
              messageLog
                  .getStyledDocument()
                  .insertString(
                      messageLog.getStyledDocument().getLength(),
                      "(" + dateFormat.format(date) + ") " + userName + ": " + input + "\n",
                      null);
              inputTextBox.setText("");
            } else {
              JOptionPane.showMessageDialog(
                  null, "Please select user you want to send your message to");
            }

          } catch (BadLocationException e) {
            e.printStackTrace();
          }
          inputTextBox.setText("");
        });

    addWindowListener(
        new WindowAdapter() {
          public void windowOpened(WindowEvent e) {
            inputTextBox.requestFocus();
          }
        });

    inputTextBox.addActionListener(this);
    send.addActionListener(this);
    file.addActionListener(this);

    add(messageLogScroller, BorderLayout.WEST);
    add(userListScroller);
    add(inputTextBoxScroller, BorderLayout.WEST);
    add(send);
    add(file);

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation(
        dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    setVisible(true);
    repaint();
    validate();
  }

  public void setUserName(String name) {
    this.userName = name;
  }

  public void updateUserList(java.util.List<String> names) {
    for (String name : names) {
      if (!userListContainer.contains(name)) {
        userListContainer.addElement(name);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Send")) {

      System.out.println("Send");
      String input = inputTextBox.getText();
      try {
        DateFormat dateFormat = new SimpleDateFormat("E HH:mm:ss");
        Date date = new Date();
        if (input.length() != 0) {
          mainController.send(Message.messageType.ALL, "", input, false);
          messageLog
              .getStyledDocument()
              .insertString(
                  messageLog.getStyledDocument().getLength(),
                  "(" + dateFormat.format(date) + ") " + userName + ": " + input + "\n",
                  null);
          inputTextBox.setText("");
        }
      } catch (BadLocationException t) {
        t.printStackTrace();
      }
      inputTextBox.requestFocus();
      inputTextBox.setText("");
    }

    if (e.getActionCommand().equals("Send file")) {}
  }

  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof Client) {
      updateUserList(mainController.getOnlineMembers());
    }
    if (o instanceof Communicator && arg instanceof ArrayList) {
      List<String> list = (ArrayList<String>) arg;
      Date date = new Date();
      DateFormat dateFormat = new SimpleDateFormat("E HH:mm:ss");
      try {
        messageLog
            .getStyledDocument()
            .insertString(
                messageLog.getStyledDocument().getLength(),
                "(" + dateFormat.format(date) + ") " + list.get(0) + ": " + list.get(1) + "\n",
                null);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
    }
  }

  public void removeFromUserList(String name) {
    userListContainer.removeElement(name);
  }
}
