 // Custom JPasswordField with underline
// Custom JPasswordField with underline and placeholder support

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JPasswordField;

public  class LinePasswordField extends JPasswordField {
      private boolean focused = false;
      public String placeholder;
      private boolean showingPlaceholder = true;

      public LinePasswordField(String placeholder) {
          this.placeholder = placeholder;
          setBorder(null);
          setForeground(Color.GRAY);
          setText(placeholder);
          setEchoChar((char) 0);

          addFocusListener(new FocusAdapter() {
              public void focusGained(FocusEvent e) {
                  if (showingPlaceholder) {
                      setText("");
                      setEchoChar('•');  // Enable masking
                      setForeground(Color.BLACK);
                      showingPlaceholder = false;
                  }
                  focused = true;
                  repaint();
              }

              public void focusLost(FocusEvent e) {
                  if (getPassword().length == 0) {
                      setText(placeholder);
                      setEchoChar((char) 0); // Disable masking
                      setForeground(Color.GRAY);
                      showingPlaceholder = true;
                  }
                  focused = false;
                  repaint();
              }
          });
      }

      @Override
      protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2 = (Graphics2D) g;
          g2.setColor(focused ? new Color(30, 144, 255) : Color.GRAY);
          g2.fillRect(0, getHeight() - 2, getWidth(), 2);
      }

      public boolean isPlaceholderActive() {
          return showingPlaceholder;
      }
  }