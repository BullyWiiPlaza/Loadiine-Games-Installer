package com.bullywiihacks.loadiine.sdcard.installer;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class WiiUGamesInstaller
{
	private SDCardManager sdCardManager;
	private SourceGameManager sourceGameManager;

	public WiiUGamesInstaller(String sdCardRoot, String sourceGameFolder, String gameName) throws IOException, SAXException, ParserConfigurationException
	{
		sourceGameManager = new SourceGameManager(sourceGameFolder);
		sdCardManager = new SDCardManager(sdCardRoot, gameName);
	}

	public void installGame() throws IOException, InterruptedException, ClassNotFoundException
	{
		String sourceDataFolder = sourceGameManager.getDataFolder();
		sdCardManager.synchronizeDataFolder(sourceDataFolder);
	}
}