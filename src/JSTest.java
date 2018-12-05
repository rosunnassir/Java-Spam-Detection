
import com.kanishka.virustotal.dto.FileScanReport;
import com.kanishka.virustotal.dto.VirusScanInfo;
import com.kanishka.virustotal.exception.APIKeyNotFoundException;
import com.kanishka.virustotal.exception.InvalidArguentsException;
import com.kanishka.virustotal.exception.QuotaExceededException;
import com.kanishka.virustotal.exception.UnauthorizedAccessException;
import com.kanishka.virustotalv2.VirusTotalConfig;
import com.kanishka.virustotalv2.VirustotalPublicV2;
import com.kanishka.virustotalv2.VirustotalPublicV2Impl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class JSTest {
//key 91f9514c2ec90243f0bb7fcf8401370b86ba7d305a9c63b77a22a3ddcee4a9d1

    private static WebDriver driver = null;
    private static JavascriptExecutor js;
    static String pageLoadStatus = null;
    ArrayList<String> readHostFile = new AdsBlocker().readHostFile();
    int count = 0;

    public String getReport(String[] urls) {
        String[] words = {"spam", "malicious", "unrated"};
        int count = 0;
        final String APIKEY = "91f9514c2ec90243f0bb7fcf8401370b86ba7d305a9c63b77a22a3ddcee4a9d1";
        try {
            VirusTotalConfig.getConfigInstance().setVirusTotalAPIKey(APIKEY);
            VirustotalPublicV2 virusTotalRef = new VirustotalPublicV2Impl();
            FileScanReport[] reports = virusTotalRef.getUrlScanReport(urls, true);
            for (FileScanReport report : reports) {
                if (report.getResponseCode() == 0) {
                    continue;
                }
                Map<String, VirusScanInfo> scans = report.getScans();
                for (String key : scans.keySet()) {
                    VirusScanInfo virusInfo = scans.get(key);
                    String res = virusInfo.getResult();
                    for (String word : words) {
                        if (res.contains(word)) {
                            count++;
                        }
                    }
                }
            }
        } catch (APIKeyNotFoundException | InvalidArguentsException | QuotaExceededException | UnauthorizedAccessException | IOException ex) {
            Logger.getLogger(JSTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count + " Web Scanner marked this page as spam.";
    }

    public void execute(String args) {
        if (!args.startsWith("http")) {
            args = "http://" + args;
        }

        System.setProperty("phantomjs.binary.path", "F:\\Miac\\as.exe");
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", false);
        driver = new PhantomJSDriver(dCaps);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        driver.get(args);
        waitForPageToLoad();
        waitSeconds(5); //make sure waitForPageToLoad has finished and demonstrated
        String[] links = getLinks(driver);
        driver.quit();
    }

    public void waitForPageToLoad() {
        do {
            js = (JavascriptExecutor) driver;
            pageLoadStatus = (String) js.executeScript("return document.readyState");
            System.out.print(".");
        } while (!pageLoadStatus.equals("complete"));
        System.out.println();
        System.out.println("Page Loaded.");
    }

    public void waitSeconds(int secons) {
        System.out.print("Pausing for " + secons + " seconds: ");
        try {
            Thread.currentThread();
            int x = 1;
            while (x <= secons) {
                Thread.sleep(1000);
                System.out.print(" " + x);
                x = x + 1;
            }
            System.out.print('\n');
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public String[] getLinks(WebDriver driver) {
        //Getting all the links present in the page by a HTML tag.
        java.util.List<WebElement> links = driver.findElements(By.tagName("a"));
        //Printing the size, will print the no of links present in the page.
        System.out.println("Total Links present is " + links.size());
        String link[] = new String[links.size()];
        //Printing the links in the page, we get through the href attribute.
        for (int i = 0; i < links.size(); i++) {
            link[i] = links.get(i).getAttribute("href");
            isads(link[i]);
        }
        return link;
    }

    public void isads(String url) {
        try {
            URL u = new URL(url);
            String domain = u.getHost();
            ArrayList< String> list = readHostFile;
            System.out.println("Size " + list.size());
            for (int i = 0; i < list.size(); i++) {
                String string = list.get(i);
                if (string.contains(domain)) {
                    System.out.println("Ads Detected");
                    count++;
                    break;
                }
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(AdsBlocker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
