package ch.m3ts.util;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ch.m3ts.tabletennis.Table;
import ch.m3ts.tabletennis.helper.Side;

public class XMLLoader {
    private XMLLoader() {
    }

    /**
     * Tries to load the serving side from an xml file from assets.
     *
     * @param videoFileName - Full name of video file in phones Camera dir. Example: "bounce_back_1.mp4"
     */
    public static Side loadServingSide(String videoFileName, AssetManager assetManager) {
        Side servingSide = Side.LEFT;
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = assetManager.open(fileNameWithoutExtension + ".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            if (properties.containsKey("servingSide") && properties.getProperty("servingSide").equals("RIGHT"))
                servingSide = Side.RIGHT;
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
        return servingSide;
    }

    /**
     * Tries to load the table location from an xml file from assets.
     *
     * @param videoFileName - Full name of video file in phones Camera dir. Example: "bounce_back_1.mp4"
     */
    public static Table loadTable(String videoFileName, AssetManager assetManager) {
        String fileNameWithoutExtension = videoFileName.split("\\.")[0];
        try (InputStream is = assetManager.open(fileNameWithoutExtension + ".xml")) {
            Properties properties = new Properties();
            properties.loadFromXML(is);
            return Table.makeTableFromProperties(properties);
        } catch (IOException ex) {
            Log.e(ex.getMessage(), ex);
        }
        return null;
    }
}
