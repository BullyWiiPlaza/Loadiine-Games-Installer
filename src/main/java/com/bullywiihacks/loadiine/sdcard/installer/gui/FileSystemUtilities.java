package com.bullywiihacks.loadiine.sdcard.installer.gui;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSystemUtilities
{
	/**
	 * Removes illegal characters from the given <code>fileName</code> to make it a valid file name
	 *
	 * @param fileName The String to sanitize
	 * @return The valid file name
	 */
	public static String sanitize(String fileName)
	{
		if (fileName == null)
		{
			return "";
		}

		// Make sure that line breaks are replaced by spaces for style reasons
		fileName = fileName.replace("\n", " ");

		if (SystemUtils.IS_OS_LINUX)
		{
			return fileName.replaceAll("/+", "").trim();
		}

		return fileName.replaceAll("[\u0001-\u001f<>:\"/\\\\|?*\u007f]+", "").trim();
	}

	/**
	 * Makes sure that a filename is valid on Windows.
	 *
	 * @param fileName The filename to check
	 * @return Whether the filename is valid or not
	 * @see <a href="http://stackoverflow.com/a/6804755/3764804">StackOverflow</a>
	 */
	public static boolean isValid(String fileName)
	{
		fileName = fileName.trim();

		Pattern pattern = Pattern.compile(
				"# Match a valid Windows filename (unspecified file system).          \n" +
						"^                                # Anchor to start of string.        \n" +
						"(?!                              # Assert filename is not: CON, PRN, \n" +
						"  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
						"    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
						"    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
						"  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
						"  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
						"  $                              # and end of string                 \n" +
						")                                # End negative lookahead assertion. \n" +
						"[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
						"[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
						"$                                # Anchor to end of string.            ",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
		Matcher matcher = pattern.matcher(fileName);

		return matcher.matches();
	}

	public static String getCurrentDirectory()
	{
		return System.getProperty("user.dir");
	}

	public static String getOperatingSystemPartitionLetter()
	{
		File[] roots = File.listRoots();
		return roots[0].toString();
	}

	public static void waitForAvailability(String absolutePath)
	{
		while (true)
		{
			if (isAvailable(absolutePath.substring(0, 3)))
			{
				break;
			}
		}
	}

	public static boolean isAvailable(String fileSystemRoot)
	{
		boolean available = false;
		File[] roots = File.listRoots();

		for (File root : roots)
		{
			if (root.getAbsolutePath().equals(fileSystemRoot))
			{
				available = true;
			}
		}

		return available;
	}
}