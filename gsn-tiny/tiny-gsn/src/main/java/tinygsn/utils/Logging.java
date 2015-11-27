package tinygsn.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.epfl.locationprivacy.util.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Logging utils
 */
public class Logging {

	//===========================================================================
	//========================= Logging Methods =================================
	//===========================================================================

	public static String filePathPart1 = "TinyGSN";
	public static String filePathPart2;

	public static String createNewLoggingFolder(Context context, String task) {
		filePathPart2 = task + "_" + System.currentTimeMillis();
		File path;
		if (isExternalStorageWritable()) {
			path = context.getExternalFilesDir(null);
		} else {
			path = context.getFilesDir();
		}
		File completePath = new File(path, filePathPart1 + File.separator + filePathPart2);
		completePath.mkdirs();
		return filePathPart2;
	}

	public static void appendLog(String subFolder, String fileName, String text, Context context) {
		File path;
		if (isExternalStorageWritable()) {
			path = context.getExternalFilesDir(null);
		} else {
			path = context.getFilesDir();
		}
		File completePath = new File(path, filePathPart1 + File.separator + subFolder);
		File logFile = new File(completePath, fileName);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "Problem with logging in TinyGSN", Toast.LENGTH_SHORT);
				e.printStackTrace();
			}
		}
		try {
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, "Problem with logging in TinyGSN", Toast.LENGTH_SHORT);
			e.printStackTrace();
		}
	}

	/**
	 * Checks if external storage is available for read and write
	 **/
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
}
