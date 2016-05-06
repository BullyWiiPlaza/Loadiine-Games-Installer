package com.bullywiihacks.loadiine.sdcard.installer.gui;

import com.bullywiihacks.loadiine.sdcard.installer.SourceGameManager;
import com.bullywiihacks.loadiine.sdcard.installer.WiiUGamesInstaller;
import com.bullywiihacks.loadiine.sdcard.installer.volume.VolumeInformation;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class WiiUGamesInstallerGUI extends JFrame
{
	private JTextField gameFolderRootPathField;
	private JButton gameFolderBrowseButton;
	private JButton sdCardBrowseButton;
	private JTextField sdCardRootPathField;
	private JPanel rootPanel;
	private JButton transferButton;
	private JTextField gameNameField;
	private JProgressBar transferProgressBar;
	private SimpleProperties persistentSettings;
	private static WiiUGamesInstallerGUI wiiUGamesInstallerGUI;
	private boolean isSDCardFormattedCorrectly;

	private WiiUGamesInstallerGUI() throws IOException
	{
		persistentSettings = new SimpleProperties();

		setFrameProperties();
		customizeTransferBar();
		restoreBackedUpSettings();
		addDataBackupShutdownHook();
		addSDCardDocumentListener();
		addSDCardBrowseButtonListener();
		addTransferButtonListener();
		addGameFolderBrowseButtonListener();
		addGameNameFieldDocumentListener();
		handleTransferButtonAvailability(false);
		checkSDCardFormatting(true);
	}

	private void addSDCardDocumentListener()
	{
		sdCardRootPathField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				checkSDCardFormatting(false);
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				checkSDCardFormatting(false);
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				checkSDCardFormatting(false);
			}
		});
	}

	private void checkSDCardFormatting(boolean initialCheck)
	{
		String sdCardRootPath = sdCardRootPathField.getText();

		if (SystemUtils.IS_OS_WINDOWS)
		{
			if (sdCardRootPath.length() >= 1)
			{
				String volumeType = VolumeInformation.getVolumeType(sdCardRootPath.charAt(0));
				String desiredVolumeType = "FAT32";

				if (!volumeType.equals(desiredVolumeType))
				{
					isSDCardFormattedCorrectly = false;
					sdCardRootPathField.setBackground(Color.RED);

					if (!initialCheck)
					{
						Object[] options = {"Yes",
								"No"};
						int selectedAnswer = JOptionPane.showOptionDialog(this,
								"Your SD card is formatted as " + volumeType + " but it should be in " + desiredVolumeType + ".\nWould you like to to start the FAT32 formatting utility?",
								"Bad SD card volume type",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null,
								options,
								null);

						if (selectedAnswer == JOptionPane.YES_OPTION)
						{
							try
							{
								Desktop.getDesktop().open(new File("Fat32 Format.exe"));
							} catch (IOException exception)
							{
								exception.printStackTrace();
							}
						}
					}
				} else
				{
					isSDCardFormattedCorrectly = true;
				}
			}
		} else
		{
			isSDCardFormattedCorrectly = true;
		}
	}

	public JProgressBar getProgressBar()
	{
		return transferProgressBar;
	}

	public static WiiUGamesInstallerGUI getInstance() throws IOException
	{
		if (wiiUGamesInstallerGUI == null)
		{
			wiiUGamesInstallerGUI = new WiiUGamesInstallerGUI();
		}

		return wiiUGamesInstallerGUI;
	}

	private void customizeTransferBar()
	{
		transferProgressBar.setMinimum(0);
		transferProgressBar.setValue(0);
		transferProgressBar.setStringPainted(true);
	}

	private void restoreBackedUpSettings()
	{
		String sdCardRoot = persistentSettings.get("SD_CARD_ROOT");

		if (sdCardRoot != null)
		{
			sdCardRootPathField.setText(sdCardRoot);
		}

		String gameFolder = persistentSettings.get("GAME_FOLDER");

		if (gameFolder != null)
		{
			gameFolderRootPathField.setText(gameFolder);
		}

		String gameName = persistentSettings.get("GAME_NAME");

		if (gameName != null)
		{
			gameNameField.setText(gameName);
		}
	}

	private void addGameNameFieldDocumentListener()
	{
		gameNameField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				handleTransferButtonAvailability(false);
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				handleTransferButtonAvailability(false);
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				handleTransferButtonAvailability(false);
			}
		});
	}

	private void addGameFolderBrowseButtonListener()
	{
		gameFolderBrowseButton.addActionListener(e ->
		{
			JFileChooser gameFolderBrowseChooser = new JFileChooser();
			gameFolderBrowseChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			if (!new File(gameFolderRootPathField.getText()).exists())
			{
				gameFolderBrowseChooser.setCurrentDirectory(new File(FileSystemUtilities.getCurrentDirectory()));
			} else
			{
				gameFolderBrowseChooser.setCurrentDirectory(new File(gameFolderRootPathField.getText()));
			}

			int state = gameFolderBrowseChooser.showOpenDialog(this);
			if (state == JFileChooser.APPROVE_OPTION)
			{
				String selectedFilePath = gameFolderBrowseChooser.getSelectedFile().getAbsolutePath();
				gameFolderRootPathField.setText(selectedFilePath);
				handleTransferButtonAvailability(true);
			}
		});
	}

	private void addTransferButtonListener()
	{
		transferButton.addActionListener(e ->
		{
			transferProgressBar.setValue(0);
			transferButton.setEnabled(false);
			transferButton.setText("Processing...");

			new SwingWorker<String, String>()
			{
				@Override
				protected String doInBackground() throws Exception
				{
					try
					{
						WiiUGamesInstaller loadiineSDInstaller = new WiiUGamesInstaller(sdCardRootPathField.getText().replace("\\", ""), gameFolderRootPathField.getText(), gameNameField.getText());
						loadiineSDInstaller.installGame();
						Toolkit.getDefaultToolkit().beep();

						JOptionPane.showMessageDialog(getRootPane(),
								"The files have been added/verified successfully!",
								"Success",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception exception)
					{
						exception.printStackTrace();

						String exceptionMessage = exception.getMessage();

						JOptionPane.showMessageDialog(getRootPane(),
								exceptionMessage,
								"Error",
								JOptionPane.ERROR_MESSAGE);
					} finally
					{
						transferButton.setText("Transfer");
						transferButton.setEnabled(true);
					}

					return null;
				}
			}.execute();
		});
	}

	private void addSDCardBrowseButtonListener()
	{
		sdCardBrowseButton.addActionListener(actionEvent ->
		{
			JFileChooser sdCardRootChooser = new JFileChooser();
			sdCardRootChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			sdCardRootChooser.setCurrentDirectory(
					sdCardRootChooser.getFileSystemView().getParentDirectory(
							new File(FileSystemUtilities.getOperatingSystemPartitionLetter())));
			int state = sdCardRootChooser.showOpenDialog(this);
			if (state == JFileChooser.APPROVE_OPTION)
			{
				String selectedFilePath = sdCardRootChooser.getSelectedFile().getAbsolutePath();
				sdCardRootPathField.setText(selectedFilePath);
				handleTransferButtonAvailability(false);
			}
		});
	}

	private void addDataBackupShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			persistentSettings.put("SD_CARD_ROOT", sdCardRootPathField.getText());
			persistentSettings.put("GAME_FOLDER", gameFolderRootPathField.getText());
			persistentSettings.put("GAME_NAME", gameNameField.getText());
			persistentSettings.writeToFile();
		}));
	}

	private void setFrameProperties() throws IOException
	{
		setTitle("Loadiine Games Installer v2.0 by Bully@WiiPlaza");
		setContentPane(rootPanel);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(450, 350);
		setLocationRelativeTo(null);
		setIconImage("Icon.png", WiiUGamesInstallerGUI.class);
	}

	private void setIconImage(String fileName, Class clazz) throws IOException
	{
		InputStream imageInputStream = clazz.getResourceAsStream("/" + fileName);
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		setIconImage(bufferedImage);
	}

	private void handleTransferButtonAvailability(boolean updatedGameName)
	{
		boolean transferButtonEnabled = true;

		String sdCardRootPath = sdCardRootPathField.getText();
		String extractedGameRootPath = gameFolderRootPathField.getText();

		if (updatedGameName)
		{
			try
			{
				SourceGameManager sourceGameManager = new SourceGameManager(extractedGameRootPath);
				String gameName = sourceGameManager.getGameName();
				String sanitizedGameName = FileSystemUtilities.sanitize(gameName);
				gameNameField.setText(sanitizedGameName);
			} catch (IllegalArgumentException ignored)
			{

			} catch (Exception exception)
			{
				exception.printStackTrace();
				JOptionPane.showMessageDialog(getRootPane(),
						exception.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		if (!new File(extractedGameRootPath).exists())
		{
			transferButtonEnabled = false;
			gameFolderRootPathField.setBackground(Color.RED);
		} else if (!SourceGameManager.isExtractedGameFolder(new File(extractedGameRootPath)))
		{
			transferButtonEnabled = false;
			gameFolderRootPathField.setBackground(Color.RED);
		} else
		{
			gameFolderRootPathField.setBackground(Color.GREEN);
		}

		boolean sdCardPathValid = new File(sdCardRootPath).exists();

		if (sdCardPathValid && isSDCardFormattedCorrectly)
		{
			sdCardRootPathField.setBackground(Color.GREEN);
		} else
		{
			transferButtonEnabled = false;
			sdCardRootPathField.setBackground(Color.RED);
		}

		if (gameNameField.getText().equals(""))
		{
			transferButtonEnabled = false;
			gameNameField.setBackground(Color.RED);
		} else if (!FileSystemUtilities.isValid(gameNameField.getText()))
		{
			transferButtonEnabled = false;
			gameNameField.setBackground(Color.RED);
		} else
		{
			gameNameField.setBackground(Color.GREEN);
		}

		transferButton.setEnabled(transferButtonEnabled);
	}
}