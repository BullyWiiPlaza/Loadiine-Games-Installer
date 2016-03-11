package com.bullywiihacks.loadiine.sdcard.installer.gui;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class WiiUGamesInstallerClient
{
	private static void useSystemLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	public static void main(String[] arguments) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException, SAXException, ParserConfigurationException
	{
		useSystemLookAndFeel();
		WiiUGamesInstallerGUI.getInstance().setVisible(true);
	}
}