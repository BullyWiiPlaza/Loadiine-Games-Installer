package com.bullywiihacks.loadiine.sdcard.installer;

import org.apache.commons.lang3.SystemUtils;
import sun.plugin2.util.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SDCardManager
{
	private String gameFolder;

	public SDCardManager(String rootDirectory, String gameFolder)
	{
		/*if (!isRoot(rootDirectory))
		{
			throw new IllegalArgumentException("Not a root directory");
		}*/

		this.gameFolder = rootDirectory + "\\wiiu\\games\\" + gameFolder;
	}

	/*public static boolean isRoot(String directory)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			Path path = new File(directory).toPath();

			return path.getNameCount() == 0;
		}

		// We cannot tell
		return true;
	}*/

	public void synchronizeDataFolder(String sourceDataFolder) throws IOException, InterruptedException, ClassNotFoundException
	{
		FileSynchronization.synchronize(sourceDataFolder, gameFolder);
	}
}