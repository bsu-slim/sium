package sium.system.util;

import java.io.File;

public class FileUtils {
	
	public static void deleteFile(String path) {
		new File(path).delete();
	}

}
