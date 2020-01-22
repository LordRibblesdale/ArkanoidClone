package controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface Path {
   String base = System.getProperty("user.dir") + File.separator;
   String setPath = base + "settings.set";
}
