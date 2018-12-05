
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
import javax.swing.JTextArea;
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
    private static String pageLoadStatus = null;
    private ArrayList<String> readHostFile = new AdsBlocker().readHostFile();
    private static int count = 0;

    public String getReport(String[] urls) {
        String[] words = {"spam", "malicious", "unrated"};
        int counter = 0;
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
                            counter++;
                        }
                    }
                }
            }
        } catch (APIKeyNotFoundException | InvalidArguentsException | QuotaExceededException | UnauthorizedAccessException | IOException ex) {
            Logger.getLogger(JSTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return counter + " Web Scanner marked this page as spam.";
    }

    public void execute(String args, JTextArea result) {
        if (!args.startsWith("http")) {
            args = "http://" + args;
        }

        System.setProperty("phantomjs.binary.path", "F:\\Miac\\as.exe");
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", false);
        setDriver(new PhantomJSDriver(dCaps));
        getDriver().manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        getDriver().get(args);
        waitForPageToLoad();
        waitSeconds(5); //make sure waitForPageToLoad has finished and demonstrated
        getLinks(getDriver());
        result.append(" No. of ads detected : " + getCount() + "\n");
        getDriver().quit();
    }

    public void waitForPageToLoad() {
        do {
            setJs((JavascriptExecutor) getDriver());
            setPageLoadStatus((String) getJs().executeScript("return document.readyState"));
            System.out.print(".");
        } while (!pageLoadStatus.equals("complete"));
        System.out.println();
        System.out.println("Page Loaded.");
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void waitSeconds(int secons) {
        System.out.print("Pausing for " + secons + " seconds: ");
        try {
            Thread.currentThread();
            int x = 1;
            while (x <= secons) {
                Thread.sleep(1000);
                System.out.print(" " + x);
                x += 1;
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
            ArrayList< String> list = getReadHostFile();
            for (int i = 0; i < list.size(); i++) {
                String string = list.get(i);
                if (string.contains(domain)) {
                    System.out.println("Ads Detected");
                    setCount(getCount() + 1);
                    System.out.println("Count " + getCount());
                    break;
                }
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(AdsBlocker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the readHostFile
     */
    public ArrayList<String> getReadHostFile() {
        return readHostFile;
    }

    /**
     * @param readHostFile the readHostFile to set
     */
    public void setReadHostFile(ArrayList<String> readHostFile) {
        this.readHostFile = readHostFile;
    }

    /**
     * @return the driver
     */
    public static WebDriver getDriver() {
        return driver;
    }

    /**
     * @param aDriver the driver to set
     */
    public static void setDriver(WebDriver aDriver) {
        driver = aDriver;
    }

    /**
     * @return the js
     */
    public static JavascriptExecutor getJs() {
        return js;
    }

    /**
     * @param aJs the js to set
     */
    public static void setJs(JavascriptExecutor aJs) {
        js = aJs;
    }

    /**
     * @return the pageLoadStatus
     */
    public static String getPageLoadStatus() {
        return pageLoadStatus;
    }

    /**
     * @param aPageLoadStatus the pageLoadStatus to set
     */
    public static void setPageLoadStatus(String aPageLoadStatus) {
        pageLoadStatus = aPageLoadStatus;
    }

    /**
     * @return the count
     */
    public static int getCount() {
        return count;
    }

    /**
     * @param aCount the count to set
     */
    public static void setCount(int aCount) {
        count = aCount;
    }

}
