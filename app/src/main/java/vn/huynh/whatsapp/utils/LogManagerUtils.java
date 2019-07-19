package vn.huynh.whatsapp.utils;

import android.util.Log;

/**
 * Created by apple on 12/15/15.
 */
public class LogManagerUtils {

    public static boolean SHOW_LOG = true;
    public static boolean DEBUG = true;
    public static boolean INFO = true;
    public static boolean VERBOSE = true;
    public static boolean ERROR = true;
    private static String TITLE_DEBUG = "DEBUG: ";
    private static String TITLE_INFO = "INFO: ";
    private static String TITLE_VERBOSE = "VERBOSE: ";
    private static String TITLE_ERROR = "ERROR: ";

    public static void d(String tag, String msg) {
        if (DEBUG && SHOW_LOG) {
            Log.d(TITLE_DEBUG + tag, msg + "");
        }
    }

    public static void i(String tag, String msg) {
        if (INFO && SHOW_LOG) {
            Log.i(TITLE_INFO + tag, msg + "");
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE && SHOW_LOG) {
            Log.v(TITLE_VERBOSE + tag, msg + "");
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR && SHOW_LOG) {
            Log.e(TITLE_ERROR + tag, msg + "");
        }
    }
}
