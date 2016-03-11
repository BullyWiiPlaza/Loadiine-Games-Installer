package com.bullywiihacks.loadiine.sdcard.installer.gui;

import java.io.*;
import java.util.Properties;

public class SimpleProperties
{
	private Properties properties;
	private OutputStream propertiesWriter;

	public SimpleProperties()
	{
		String propertiesFileName = "config.properties";

		properties = new Properties();

		try
		{
			if (new File(propertiesFileName).exists())
			{
				InputStream propertiesReader = new FileInputStream(propertiesFileName);
				properties.load(propertiesReader);
			}

			propertiesWriter = new FileOutputStream(propertiesFileName);
		} catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}

	public void put(String key, String value)
	{
		properties.setProperty(key, value);

		try
		{
			properties.store(propertiesWriter, null);
		} catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}

	public String get(String key)
	{
		return (String) properties.get(key);
	}
}