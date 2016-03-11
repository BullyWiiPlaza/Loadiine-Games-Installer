package com.bullywiihacks.loadiine.sdcard.installer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ErrorLogFileWriter
{
	public static void logException(Exception exception) throws IOException
	{
		FileWriter fileWriter = new FileWriter("Errors.txt", true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
		exception.printStackTrace(printWriter);
		exception.printStackTrace();
	}
}