package twitter;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author juimanoj
 */
public class URLExtractor {

    public static String getLongUrl(String shortUrl) throws MalformedURLException, IOException {
        String result = shortUrl;
        String header;
        do {
            URL url = new URL(result);
            HttpURLConnection.setFollowRedirects(false);
            URLConnection conn = url.openConnection();
            header = conn.getHeaderField(null);
            String location = conn.getHeaderField("location");
            if (location != null) {
                result = location;
            }
        } while (header.contains("301"));

        return result;
    }
//    public static void main(String[] args) throws MalformedURLException, IOException {
//        String result = UrlExtractor.getLongUrl("http://bit.ly/gZ2N7");
//        System.out.println(result);
//    }
}
