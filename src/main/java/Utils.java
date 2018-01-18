import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class Utils {
    public static String encodeImage(String mediaUrl) {
		if (!exists(mediaUrl)) {
			System.out.println("invalid input image path: " + mediaUrl);
			return null;
		}
        try {
            byte[] imageBytes = IOUtils.toByteArray(new URL(mediaUrl));
            return Base64.encodeBase64String(imageBytes);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
        }
        return null;
    }
    
    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =  (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
