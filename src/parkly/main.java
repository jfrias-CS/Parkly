package parkly;

import java.io.IOException;

import javax.swing.SwingUtilities;
/*
 * Main driver for a client to use the application. Start of Parkly operation. 
 */
public class main {
	public static void main(String[] args) throws IOException {
		// Use SwingUtilities.invokeLater() to ensure that the GUI creation and manipulation 
		// occurs on the Event Dispatch Thread (EDT), which is mandatory for Swing applications.
		SwingUtilities.invokeLater(() -> {
			// Begin background task of creating a network connection
			System.out.println("Starting network conneciton...");
			new ConnectTask(null).execute();
		}); 
	}
}


