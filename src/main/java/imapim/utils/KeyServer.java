package imapim.utils;

import imapim.data.PubGPGKey;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyServer {

    private static KeyServer server;

    public static KeyServer getInstance() {
        if (KeyServer.server == null) {
            KeyServer.server = new KeyServer();
        }
        return KeyServer.server;
    }

    public ArrayList<PubGPGKey> searchForKey(String pattern) {
        ArrayList<PubGPGKey> keyList = null;
        Document doc = null;
        Elements elements = null;
        ArrayList<PubGPGKey> searchList = new ArrayList<PubGPGKey>();
        try {
            String url = "http://pgp.mit.edu/pks/lookup?op=index&search=" + pattern;
            Connection con = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(30000);
            // Connection.Response resp = con.execute();
            doc = con.get();
            elements = doc.select("body pre:gt(1)");
            for (Element el : elements) {
                String date = null;
                date = matchDate(el.html());
                Element link = el.selectFirst("a");
                String fingerPrint = link.attr("href");
                String userId = matchUserID(el);
                fingerPrint = fingerPrint.substring(fingerPrint.length() - 16);
                PubGPGKey newKey = new PubGPGKey(fingerPrint, userId, date);
                searchList.add(newKey);
                // System.out.print(newKey);
            }
        } catch (HttpStatusException e) {
            System.out.println(e.getStatusCode());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("IOException");
        }
        return searchList;

    }

    public String matchDate(String input) {
        Pattern datePattern = Pattern.compile("</a>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*<a\\s+href");
        Matcher m = datePattern.matcher(input);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private String matchUserID(Element el) {
        System.out.println(el.html());
        Elements links = el.select("a:gt(0)");
        if (links.size() > 0) {
            return links.text();
        } else {
            return "";
        }
    }

    public String getKeyContent(String keyID) {
        String contentURL = "http://pgp.mit.edu/pks/lookup?op=get&search=0x";
        Document doc = null;
        String pubKeyContent = "";
        try {
            doc = Jsoup.connect(contentURL + keyID).get();
            pubKeyContent = doc.selectFirst("pre").text();
            System.out.println(pubKeyContent);
        } catch (HttpStatusException e) {
            System.out.println("Error");
        } catch (IOException e) {
            System.out.println("IO Error");
        }
        return pubKeyContent;
    }

}
