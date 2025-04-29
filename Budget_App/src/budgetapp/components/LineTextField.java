// Custom JTextField with underline and placeholder support
package budgetapp.components;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;

public  class LineTextField extends JTextField
{
	//Indicates whether the field is focused
	    private boolean focused = false;
		//Placeholder text for the field
	    public String placeholder;
		//Tracks if the placeholder is currently displayed
	    private boolean showingPlaceholder = true;

		//Constructor to initialize the custom text field with a placeholder
	    public LineTextField(String placeholder) {
	        this.placeholder = placeholder;
			//Remove default border
	        setBorder(null);
			//Set placeholder text color
	        setForeground(Color.GRAY);
			//Display placeholder text initially
	        setText(placeholder);

			//Add focus listener to handle placeholder behavior
	        addFocusListener(new FocusAdapter()
			{
	            public void focusGained(FocusEvent e)
				{
					//Remove placeholder when the field gains focus
	                if (getText().equals(placeholder))
					{
						//Clear placeholder text
	                    setText("");
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
	                if (getText().isEmpty())
					{
						//Restore placeholder text
	                    setText(placeholder);
						//Set placeholder text color
	                    setForeground(Color.GRAY);
						//Update placeholder state
	                    showingPlaceholder = true;
	                }
					//Mark filed as not focused
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
		  //Draw underline with different color based on focus state
          g2.setColor(focused ? new Color(30, 144, 255) : Color.GRAY);
          g2.fillRect(0, getHeight() - 2, getWidth(), 2);
      }

	  //Method to check if the placeholder is currently active
      public boolean isPlaceholderActive()
	  {
          return showingPlaceholder || getText().equals(placeholder);
      }
  }