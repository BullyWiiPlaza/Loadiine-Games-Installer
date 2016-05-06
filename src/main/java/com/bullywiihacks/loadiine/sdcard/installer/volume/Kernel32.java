package com.bullywiihacks.loadiine.sdcard.installer.volume;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public interface Kernel32 extends StdCallLibrary
{
	Map<String, Object> WIN32API_OPTIONS = new HashMap<String, Object>()
	{
		private static final long serialVersionUID = 1L;

		{
			put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
			put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
		}
	};

	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class, WIN32API_OPTIONS);

	boolean GetVolumeInformation(
			String lpRootPathName,
			char[] lpVolumeNameBuffer,
			DWORD nVolumeNameSize,
			IntByReference lpVolumeSerialNumber,
			IntByReference lpMaximumComponentLength,
			IntByReference lpFileSystemFlags,
			char[] lpFileSystemNameBuffer,
			DWORD nFileSystemNameSize
	);
}