package com.bullywiihacks.loadiine.sdcard.installer;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class SourceGameManager
{
	private String dataFolder;
	private String metaXMLFolder;

	public SourceGameManager(String extractedGameFolder) throws ParserConfigurationException, SAXException, IOException
	{
		File sourceFolder = new File(extractedGameFolder);

		if (!isExtractedGameFolder(sourceFolder))
		{
			throw new IllegalArgumentException("code and content folders not found");
		}

		dataFolder = sourceFolder.getAbsolutePath();
		metaXMLFolder = dataFolder + File.separator + "meta" + File.separator + "meta.xml";
	}

	public static boolean isExtractedGameFolder(File sourceFolder)
	{
		return containsFolder(sourceFolder, "code") && containsFolder(sourceFolder, "content");
	}

	private static boolean containsFolder(File sourceFolder, String folderName)
	{
		File containedFolder = new File(sourceFolder.getAbsolutePath() + File.separator + folderName);

		return containedFolder.exists() && containedFolder.isDirectory();
	}

	public String getDataFolder()
	{
		return dataFolder;
	}

	public String getGameName() throws ParserConfigurationException, SAXException, IOException
	{
		GameDataParser gameDataParser = new GameDataParser(metaXMLFolder);

		return gameDataParser.getGameName() + " [" + gameDataParser.getGameId() + "]";
	}
}