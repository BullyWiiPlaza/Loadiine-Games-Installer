package com.bullywiihacks.loadiine.sdcard.installer;

import com.bullywiihacks.loadiine.sdcard.installer.gui.FileSystemUtilities;
import com.bullywiihacks.loadiine.sdcard.installer.gui.WiiUGamesInstallerGUI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.bridj.Pointer;
import org.bridj.cpp.com.COMRuntime;
import org.bridj.cpp.com.shell.ITaskbarList3;
import org.bridj.jawt.JAWTUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSynchronization
{
	private static final Logger LOGGER = Logger.getLogger(FileSynchronization.class.getName());
	public static JSlider slider;
	public static ITaskbarList3 taskBarList;
	public static Pointer<?> windowsWindowHandle;
	public static ITaskbarList3.TbpFlag taskBarProgressState;

	/**
	 * Collects all files in the source directory recursively
	 *
	 * @param sourceDirectory The directory to traverse
	 * @return A list containing all files
	 * @throws IOException
	 */
	public static List<Path> listFiles(String sourceDirectory) throws IOException
	{
		Path path = Paths.get(sourceDirectory);
		List<Path> files = new LinkedList<>();
		Files.walk(path).forEach(files::add);

		return files;
	}

	/**
	 * Mirrors the <code>sourceDirectory</code> to the <code>targetDirectory</code>. Non-existing files in the <code>targetDirectory</code> are deleted and only modified files are updated
	 *
	 * @param sourceDirectory The source directory
	 * @param targetDirectory The target directory
	 * @throws IOException
	 */
	public static void synchronize(String sourceDirectory, String targetDirectory) throws IOException, InterruptedException, ClassNotFoundException
	{
		LOGGER.setLevel(Level.OFF);

		LOGGER.info("Source directory: " + sourceDirectory);
		LOGGER.info("Target directory: " + targetDirectory);

		createFolders(targetDirectory);

		deleteRedundant(sourceDirectory, targetDirectory);
		createAndUpdate(sourceDirectory, targetDirectory);

		LOGGER.info("Synchronization finished");
	}

	/**
	 * Deletes files from the <code>targetDirectory</code> that do not exist in the <code>sourceDirectory</code>
	 *
	 * @param sourceDirectory The directory to use as source
	 * @param targetDirectory The directory to use as destination
	 * @throws IOException
	 */
	private static void deleteRedundant(String sourceDirectory, String targetDirectory) throws IOException
	{
		List<Path> targetFiles = listFiles(targetDirectory);

		for (Path targetFile : targetFiles)
		{
			LOGGER.info("Target file: " + targetFile);
			String relativePath = getRelativePath(targetFile, targetDirectory);
			LOGGER.info("Relative path: " + relativePath);

			if (!exists(sourceDirectory, relativePath))
			{
				delete(targetFile);
			}
		}
	}

	/**
	 * Mirrors the <code>sourceDirectory</code> to the <code>targetDirectory</code> by only copying files that have been modified
	 *
	 * @param sourceDirectory The directory to mirror from
	 * @param targetDirectory The directory to mirror to
	 * @throws IOException
	 */
	private static void createAndUpdate(String sourceDirectory, String targetDirectory) throws IOException, InterruptedException, ClassNotFoundException
	{
		List<Path> sourceFiles = listFiles(sourceDirectory);
		int sourceFilesCount = sourceFiles.size();
		Component component = WiiUGamesInstallerGUI.getInstance();

		if (SystemUtils.IS_OS_WINDOWS)
		{
			long hwndVal = JAWTUtils.getNativePeerHandle(component);
			windowsWindowHandle = Pointer.pointerToAddress(hwndVal);
			slider = new JSlider(0, sourceFilesCount, 0);
			taskBarList = COMRuntime.newInstance(ITaskbarList3.class);
			ITaskbarList3.TbpFlag taskBarProgressState = ITaskbarList3.TbpFlag.TBPF_NORMAL;
			taskBarList.SetProgressState((Pointer) windowsWindowHandle, taskBarProgressState);
		}

		JProgressBar progressBar = ((WiiUGamesInstallerGUI) component).getProgressBar();
		progressBar.setMaximum(sourceFilesCount);

		for (int sourceFilesIndex = 0; sourceFilesIndex < sourceFilesCount; sourceFilesIndex++)
		{
			Path sourceFile = sourceFiles.get(sourceFilesIndex);
			LOGGER.info("Current source file: " + sourceFile);

			String relativeSourcePath = getRelativePath(sourceFile, sourceDirectory);
			String absoluteTargetPath = targetDirectory + relativeSourcePath;

			LOGGER.info("Current target path: " + absoluteTargetPath);

			// Keep doing the IO operation till it succeeds
			while (true)
			{
				try
				{
					FileSystemUtilities.waitForAvailability(absoluteTargetPath);

					if (sourceFile.toFile().isDirectory())
					{
						createFolders(absoluteTargetPath);
					} else if (!fileContentEquals(sourceFile, absoluteTargetPath))
					{
						robustFileCopy(sourceFile, absoluteTargetPath);
					}

					// It succeeded
					break;
				} catch (Exception exception)
				{
					ErrorLogFileWriter.logException(exception);
				}
			}

			progressBar.setValue(sourceFilesIndex + 1);

			if (SystemUtils.IS_OS_WINDOWS)
			{
				slider.setValue(sourceFilesIndex + 1);
				taskBarList.SetProgressValue((Pointer) windowsWindowHandle, slider.getValue(), slider.getMaximum());
			}
		}

		progressBar.setValue(sourceFilesCount);

		if (SystemUtils.IS_OS_WINDOWS)
		{
			slider.setValue(sourceFilesCount);

			taskBarProgressState = ITaskbarList3.TbpFlag.TBPF_NOPROGRESS;
			taskBarList.SetProgressState((Pointer) windowsWindowHandle, taskBarProgressState);
		}
	}

	/**
	 * Creates the folder including parent folders if it doesn't exist yet
	 *
	 * @param folderPath The folder to create
	 */
	private static void createFolders(String folderPath) throws IOException
	{
		if (!new File(folderPath).exists())
		{
			Files.createDirectory(Paths.get(folderPath));
		}
	}

	/**
	 * Determines whether two files are equal content-wise
	 *
	 * @param sourceFile The first file
	 * @param targetFile The second file
	 * @return True if they are equal false otherwise
	 * @throws IOException
	 */
	private static boolean fileContentEquals(Path sourceFile, String targetFile) throws IOException
	{
		return FileUtils.contentEquals(sourceFile.toFile(), new File(targetFile));
	}

	/**
	 * Copies the file <code>sourceFile</code> to <code>targetFile</code>
	 *
	 * @param sourceFile The file to copy
	 * @param targetFile The target file
	 * @throws IOException
	 */
	private static void robustFileCopy(Path sourceFile, String targetFile) throws IOException, InterruptedException
	{
		LOGGER.info("Copying " + sourceFile + " to " + targetFile + " ...");
		Files.copy(sourceFile, Paths.get(targetFile), StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Deletes a file or folder
	 *
	 * @param path The file path
	 * @throws IOException
	 */
	private static void delete(Path path) throws IOException
	{
		LOGGER.info("Deleting file " + path + " ...");
		FileUtils.forceDelete(path.toFile());
	}

	/**
	 * Returns the relative path of a <code>file</code> regarding the <code>startingDirectory</code>.
	 *
	 * @param file              The file representing the full path
	 * @param startingDirectory The directory to start at
	 * @return The relative path from the <code>startingDirectory</code> to the full <code>file</code> path
	 */
	private static String getRelativePath(Path file, String startingDirectory)
	{
		return file.toString().replace(startingDirectory, "");
	}

	/**
	 * Determines whether the <code>sourceDirectory</code> including a <code>relativePath</code> exists or not
	 *
	 * @param sourceDirectory The directory to start at
	 * @param relativePath    The path to dig
	 * @return True whether the newly constructed path exists false otherwise
	 */
	private static boolean exists(String sourceDirectory, String relativePath)
	{
		String sourceFile = sourceDirectory + relativePath;
		boolean exists = new File(sourceFile).exists();
		LOGGER.info("Source file " + sourceFile + " exists: " + exists);

		return exists;
	}
}