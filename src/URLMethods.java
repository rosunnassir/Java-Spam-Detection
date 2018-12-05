
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Calendar;
import javax.swing.JTextArea;
import org.jsoup.*;
import org.jsoup.Connection.Response;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class URLMethods {

    public static String URLCheck(String str) {
        Pattern urlPattern = Pattern.compile("((https?|http|telnet):((//)|(\\\\\\\\))+[\\\\w\\\\d:#@%/;$()~_?\\\\+-=\\\\\\\\\\\\.&]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(str);
        if (matcher.find() == false) {
            str = "https://".concat(str);
        }
        return str;
    }

    public String readURL(String address) {
        StringBuilder source = new StringBuilder();
        String tempstr;
        final String link = URLCheck(address);
        if (link != null) {
            try {
                URL u = new URL(link);
                BufferedReader c1;
                try (InputStream in = new BufferedInputStream(u.openStream())) {
                    c1 = new BufferedReader(new InputStreamReader(in));
                    while ((tempstr = c1.readLine()) != null) {
                        source.append(tempstr);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(URLMethods.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(URLMethods.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return source.toString();
    }

    public boolean isSpam(String args, String dns) {

        try {
            byte[] net = InetAddress.getByName(args).getAddress();
            String query = dns;
            for (byte b : net) {
                int unsigned = b < 0 ? b + 256 : b;
                query = unsigned + "." + query;
            }
            System.out.println("Query " + query);
            InetAddress temp = InetAddress.getByName(query);
            return true;
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    public boolean isSpamDns(String address, JTextArea result) {
        String dnsQuery[] = {"sbl.spamhaus.org", "ips.backscatterer.org", "pbl.spamhaus.org"};
        for (String string : dnsQuery) {
            boolean spam = isSpam(address, string);
            if (spam) {
                return true;
            }
        }
        return false;
    }

    public void getRedirect(String address, JTextArea result) {
        Thread runns = new Thread() {
            @Override
            public void run() {
                int count = 0;
                try {
                    String url = URLCheck(address);
                    Response response = Jsoup.connect(url).followRedirects(false).execute();
                    if (response.hasHeader("location")) {
                        ++count;
                        System.out.println("counter" + count);
                        String redirectUrl = response.header("location");
                        ++count;
                        result.append("Redirect URL is " + redirectUrl + "\n");
                        getRedirect(redirectUrl, result);
                    }
                } catch (IOException ex) {
                    result.append("Connection Not Secure");
                }
                result.append(count + " Redirects.\n");
            }
        };
        runns.start();
    }

    public String performWhoisQuery(String query) {
        StringBuilder c = new StringBuilder();
        try {
            String domainName = getDomainName(query);
            int port = 43;
            String host = "whois.internic.net";
            System.out.println("Performing Whois Query.");
            Socket socket = new Socket(host, port);
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader in = new BufferedReader(isr);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(domainName);
            String line = "";
            String compareDate = "";
            String compareDate2 = "";
            while ((line = in.readLine()) != null) {
                if (line.startsWith("For more")) {
                    break;
                } else if (line.contains("Updated Date: ")) {
                    int i1 = line.indexOf(":") + 2;
                    int i2 = i1 + 11;
                    String date = line.substring(i1, i2);
                    compareDate = "Domain Server was updated " + compareDateUpdate(date, true) + " Months ago.";
                } else if (line.contains("Expiry Date:")) {
                    int i1 = line.indexOf(":") + 2;
                    int i2 = i1 + 11;
                    String date = line.substring(i1, i2);
                    compareDate2 = "Domain Server will expires in " + compareDateUpdate(date, false) + " Months.";
                }
            }
            c.append(compareDate).append("\n").append(compareDate2).append("\n");
        } catch (IOException ex) {
            Logger.getLogger(URLMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c.toString();
    }

    public String getDomainName(String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            URI uri = new URI(url);
            String domain = uri.getHost();
            System.out.println("domain: " + domain);
            if (uri.getScheme() != null) {
                String domainDef = domain.startsWith("www.") ? domain.substring(4) : domain;

                return domainDef;
            } else {
                return uri.getSchemeSpecificPart();
            }
        } catch (URISyntaxException e) {
            System.out.println(e);
            return null;
        }
    }

    public int compareDateUpdate(String date, boolean t) {
        int dif = 0;
        String rete = date.substring(5, 7);
        int month = Integer.parseInt(rete);
        int upyr = Integer.parseInt(date.substring(0, 4));
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int diffyr = upyr - year;
        int date2 = Calendar.getInstance().get(Calendar.MONTH);
        if (t == true) {
            if (diffyr != 0) {

                dif = diffyr * 12;
            } else {
                dif = date2 - month;
            }
        } else if (t == false) {
            if (diffyr != 0) {
                int difmon = (diffyr * 12) - date2;
                dif = difmon + month;
            } else {
            }
        }
        return dif;
    }

    public void Extract(String args[], JTextArea result) {
        Thread runns = new Thread() {
            @Override
            public void run() {
                try {
                    Validate.isTrue(args.length == 1, "usage: supply url to fetch");
                    String url = URLCheck(args[0]);
                    Document doc = Jsoup.connect(url).get();
                    Elements media = doc.select("script[src]");
                    result.append("Total Script found : " + media.size());
                    media.forEach((Element src) -> {
                        String execute = src.attr("abs:src");
                        System.out.println("Script " + execute);
                    });
                } catch (IOException ex) {
                    String url = "http://" + args[0];
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(url).get();
                    } catch (IOException ex1) {
                        Logger.getLogger(URLMethods.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    Elements media = doc.select("script[src]");
                    result.append("Total Script found : " + media.size());
                    media.forEach((Element src) -> {
                        String execute = src.attr("abs:src");
                        System.out.println("Script " + execute);
                    });
                }
            }
        };
        runns.start();
    }

    public void analyseURL(String input, JTextArea result) {

        final String url = input;
        final int length = url.length();
        final String SLASH = "/";
        final String ADD = "@";
        final String DOT = ".";

        Thread Check;
        Check = new Thread() {
            @Override
            public void run() {
                int countAlpha = 0;
                int countNumeric = 0;
                int addCount = url.length() - url.replaceAll(ADD, "").length();
                int slashCount = url.length() - url.replaceAll(SLASH, "").length();
                int dotCount = url.length() - url.replaceAll(DOT, "").length();
                /////////////////////////////////////////////////////////////////////////////////////
                if (dotCount > 3) {
                    System.out.println("URL Contains more than 3 dots.");
                }
                if (slashCount > 3 && length < 15) {
                    System.out.println("URL Contains more than 3 slashes.");
                }
                if (addCount != 0) {
                    int index = url.indexOf(ADD);
                    String redir = url.substring(index);
                    System.out.println("URL Contains hidden link.\nDestination is " + redir);
                }
                ////////////////////////////////////////////////////////////////////////////////////
                for (int i = 0; i < length; i++) {
                    char temp = url.charAt(i);
                    try {
                        int z = Integer.parseInt("" + temp);
                        countNumeric++;
                    } catch (NumberFormatException e) {
                        countAlpha++;
                    }
                }
                /////////////////////////////////////////////////////////////////////////////////////
                if (slashCount < 3 && length >= 15) {
                    System.out.println("Link is abnormally long with " + countNumeric + " numbers and " + countAlpha + " letters");
                }
            }
        };
        Check.start();
    }

}
