import com.bullywiihacks.loadiine.sdcard.installer.gui.WiiUGamesInstallerGUI;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class WiiUGamesInstallerClient
{
	public static void main(String[] arguments) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SwingUtilities.invokeLater(() ->
		{
			try
			{
				WiiUGamesInstallerGUI gamesInstallerGUI = WiiUGamesInstallerGUI.getInstance();
				gamesInstallerGUI.setVisible(true);
			} catch (IOException exception)
			{
				exception.printStackTrace();
			}
		});
	}
}