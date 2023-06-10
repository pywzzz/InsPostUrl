import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.*;

public class SeleniumScraper {
    private static final String INSTAGRAM_URL = "https://www.instagram.com";
    private static final String CHROME_DRIVER_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe";
    private static Map<String, ArrayList<String>> userPostUrls = new HashMap<>();
    private static final ArrayList<String> users = new ArrayList<>(
            Arrays.asList("yuqisong.923", "noodle.zip", "yeh.shaa_", "tiny.pretty.j", "min.nicha")
    );

    static {
        try {
            initUserPostUrls(userPostUrls, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 0表示第一次执行时的延迟为0即上去就执行，之后是每12小时执行一次
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ArrayList<String> allPostUrls = getAllUsersPostUrls();
                for (String postUrl : allPostUrls) {
                    // 执行发送的业务
                    ImageSearch.submissionByUrl(postUrl);
                }
            }
        }, 0, 12 * 60 * 60 * 1000);
    }

    public static void initUserPostUrls(Map<String, ArrayList<String>> userPostUrls, ArrayList<String> users) throws IOException {
        for (String user : users) {
            userPostUrls.put(user, getPostUrlsByUsername(user));
        }
    }

    public static ArrayList<String> getAllUsersPostUrls() {
        ArrayList<String> allPostUrls = new ArrayList<>();
        for (String user : users) {
            try {
                // 获取最新的帖子url数组
                ArrayList<String> latestPostUrls = getPostUrlsByUsername(user);
                // 将新的url数组和旧的url数组对比，筛选出新的中相比于旧的中那些多出来的，也即刚发的那些url
                ArrayList<String> finalPostUrls = updatePostUrls(userPostUrls.get(user), latestPostUrls);
                // 将旧的url数组更新为最新的
                userPostUrls.put(user, latestPostUrls);
                // 把这些帖子链接放入到allPostUrls中（finalPostUrls可能为空，但不影响放入到allPostUrls中）
                allPostUrls.addAll(finalPostUrls);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allPostUrls;
    }

    public static ArrayList<String> getPostUrlsByUsername(String username) throws IOException {
        // 使用时先下载chrome，然后再下载对应版本的chromedriver.exe，将这个exe放到和chrome相同的文件夹下
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        Proxy proxy = new Proxy();
        proxy.setHttpProxy("127.0.0.1:7890");
        proxy.setSslProxy("127.0.0.1:7890");
        ArrayList<String> latestPostUrls = new ArrayList<>();
        String userUrl = INSTAGRAM_URL + "/" + username;

        ChromeOptions options = new ChromeOptions();
        options.setCapability("proxy", proxy);
        // options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);
        driver.get(userUrl);

        // 暂停程序执行10秒来等待网页加载完成
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String pageSource = driver.getPageSource();

        Document doc = Jsoup.parse(pageSource);
        // class属性为_aabd、_aa8k、_aanf的div标签
        Elements divs = doc.select("div._aabd._aa8k._aanf");
        for (Element div : divs) {
            // 从每个div中选择第一个a标签
            Element a = div.selectFirst("a");
            if (a != null) { // 防止空指针异常
                // 打印a标签的href属性值
                String postUrl = INSTAGRAM_URL + a.attr("href");
                latestPostUrls.add(postUrl);
            }
        }
        driver.quit();
        return latestPostUrls;
    }

    public static ArrayList<String> updatePostUrls(ArrayList<String> oldPostUrls, ArrayList<String> newPostUrls) {
        ArrayList<String> finalPostUrls = new ArrayList<>();
        Set<String> setOldPostUrls = new LinkedHashSet<>(oldPostUrls);

        for (String element : newPostUrls) {
            if (!setOldPostUrls.contains(element)) {
                finalPostUrls.add(element);
            }
        }

        return finalPostUrls;
    }
}

