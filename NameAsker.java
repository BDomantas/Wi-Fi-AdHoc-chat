package Routing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NameAsker extends JDialog {
  boolean closed = false;
  private Controller mainController;
  private JPanel contentPane;
  private JButton buttonOK;
  private JTextField usernameField;
  ActionListener listener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!getClientName().equals("") && !getClientName().startsWith(" ")) {
            System.out.println(getClientName());
            try {
              mainController.showMain(getClientName());
            } catch (Exception e1) {
              e1.printStackTrace();
            }
            close();
          }
        }
      };
  private JLabel label;
  private String clientName;

  public NameAsker(Controller controller) {
    setTitle("Chat of group 20");
    Font f = new Font("Font", Font.ITALIC, 13);
    mainController = controller;
    setFont(f);
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    buttonOK.addActionListener(listener);
    pack();
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(
        dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    setVisible(true);
  }

  public String getClientName() {
    return usernameField.getText();
  }

  public void close() {
    closed = true;
    dispose();
  }

  public boolean isClosed() {
    return closed;
  }
}
