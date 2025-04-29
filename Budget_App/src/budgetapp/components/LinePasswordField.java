// Custom JPasswordField with underline and placeholder support
package budgetapp.components;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JPasswordField;

public  class LinePasswordField extends JPasswordField
{
    //Indicates whether the field is focused
      private boolean focused = false;
      //Placeholder text for the field
      public String placeholder;
      //Tracks if the placeholder is currently displayed
      private boolean showingPlaceholder = true;

      //Constructor to initialize the custom password field with a placeholder
      public LinePasswordField(String placeholder) {
          this.placeholder = placeholder;
          //Removes default border
          setBorder(null);
          //Set placeholder text color
          setForeground(Color.GRAY);
          //Display placeholder text initially
          setText(placeholder);
          //Diable masking for placeholder
          setEchoChar((char) 0);

          //Add focus listener to handle placeholder behavior
          addFocusListener(new FocusAdapter() {
              public void focusGained(FocusEvent e)
              {
                  //Remove placeholder when the field gains focus
                  if (showingPlaceholder)
                  {
                      //Clear placeholder text
                      setText("");
                      //Enable masking
                      setEchoChar('•');
                      //Set text color to black
                      setForeground(Color.BLACK);
                      //Update placeholder state
                      showingPlaceholder = false;
                  }
                  //Mark field as focused
                  focused = true;
                  //Repaint to update underline color
                  repaint();
              }

              public void focusLost(FocusEvent e)
              {
                  //Restore placeholder if the field is empty when focus is lost
                  if (getPassword().length == 0)
                  {
                      //Restore placeholder text
                      setText(placeholder);
                      //Disable masking
                      setEchoChar((char) 0);
                      //Set placeholder text color
                      setForeground(Color.GRAY);
                      //Update placeholder state
                      showingPlaceholder = true;
                  }
                  //Mark field as not focused
                  focused = false;
                  //Repaint to update underline color
                  repaint();
              }
          });
      }

      @Override
      protected void paintComponent(Graphics g)
      {
          super.paintComponent(g);
          Graphics2D g2 = (Graphics2D) g;
          //Draw underline with different color based one focus state
          g2.setColor(focused ? new Color(30, 144, 255) : Color.GRAY);
          g2.fillRect(0, getHeight() - 2, getWidth(), 2);
      }

      //Method to check if the placeholder is currently active
      public boolean isPlaceholderActive()
      {
          return showingPlaceholder;
      }
  }