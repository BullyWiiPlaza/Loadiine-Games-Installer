package com.bullywiihacks.loadiine.sdcard.installer.volume;

import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.ptr.IntByReference;

public class VolumeInformation
{
	public static String getVolumeType(char driveLetter)
	{
		char[] lpVolumeNameBuffer = new char[256];
		DWORD nVolumeNameSize = new DWORD(256);
		IntByReference lpVolumeSerialNumber = new IntByReference();
		IntByReference lpMaximumComponentLength = new IntByReference();
		IntByReference lpFileSystemFlags = new IntByReference();

		char[] lpFileSystemNameBuffer = new char[256];
		DWORD nFileSystemNameSize = new DWORD(256);

		lpVolumeSerialNumber.setValue(0);
		lpMaximumComponentLength.setValue(256);
		lpFileSystemFlags.setValue(0);

		Kernel32.INSTANCE.GetVolumeInformation(
				driveLetter + ":\\",
				lpVolumeNameBuffer,
				nVolumeNameSize,
				lpVolumeSerialNumber,
				lpMaximumComponentLength,
				lpFileSystemFlags,
				lpFileSystemNameBuffer,
				nFileSystemNameSize);

		String fs = new String(lpFileSystemNameBuffer);
		return fs.trim();
	}
}