package de.codenis.mdcs;

import android.os.Environment;

public class Config {
	 // Server url
    public static String SERVER_URL= "https://bwxpress.ch/mdcs/API/";
    public static String SERVER_FILES_ACCESS_URL= "https://bwxpress.ch/mdcs/API/getFiles.php?hash=3b90415cca1ae640d5c6be23f30dd0a614cd0866";
    public static String pdfDir = ""+Environment.getExternalStorageDirectory()+"/mdcsPdf/";
    public static String imageDir = ""+Environment.getExternalStorageDirectory()+"/Pictures/mdcsPhoto/";
    public static String rpObject = ""+Environment.getExternalStorageDirectory()+"/Pictures/RP_object/";
    public static String rpPosition1 = ""+Environment.getExternalStorageDirectory()+"/Pictures/RP_position1/";
    public static String rpPosition2 = ""+Environment.getExternalStorageDirectory()+"/Pictures/RP_position2/";

}
