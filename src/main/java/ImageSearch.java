import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageSearch {
    private static final double scaling = 1.25;
    private static final Robot robot;

    static {
        // 加载OpenCV库（这儿使用的是3.4.16版本）
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void submissionByUrl(String postUrl) {
        // 5秒后启动
        robot.delay(5000);

        // 发送帖子链接
        pasteText(postUrl);
        pressEnter();

        // 等待别人发消息
        robot.delay(10000);

        // 截图
        BufferedImage screenshot = captureScreen(598, 650, 800, 316);
        // 路径
        File screenshotFile = new File("src/main/resources/image/screenshot.png");
        // 保存截图
        saveScreenshot(screenshot, screenshotFile);

        // 使用OpenCV加载截图
        Mat screenshotMat = Imgcodecs.imread(screenshotFile.getAbsolutePath());
        // 使用OpenCV加载箭头
        Mat arrowMat = Imgcodecs.imread("src/main/resources/image/arrow.jpg");
        // 使用模板匹配在截图中找到箭头
        Mat result = new Mat();
        Imgproc.matchTemplate(screenshotMat, arrowMat, result, Imgproc.TM_CCOEFF);
        // 找到匹配的位置
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

        // 移位置并点击
        moveToAndClick((int) ((matchLoc.x + 598 + 5) / scaling), (int) ((matchLoc.y + 650 + 5) / scaling));

        robot.delay(1000);

        // 搜索金珍妮
        pasteText("金珍妮");

        robot.delay(1000);

        // 选中金珍妮
        moveToAndClick((int) (800 / scaling), (int) (325 / scaling));

        robot.delay(1000);

        // 移位置并点击发送按钮
        moveToAndClick((int) (1130 / scaling), (int) (865 / scaling));

        robot.delay(1000);

        // 进入金珍妮界面
        moveToAndClick((int) (330 / scaling), (int) (140 / scaling));

        robot.delay(1000);

        // 再点一下金珍妮界面
        clickMouse();

        // 等待金珍妮回消息
        robot.delay(10000);

        // 点击匿名
        moveToAndClick((int) (760 / scaling), (int) (835 / scaling));

        robot.delay(1000);

        // 回到最初界面
        moveToAndClick((int) (330 / scaling), (int) (220 / scaling));
    }

    // 按回车键
    private static void pressEnter() {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    // 传入文本然后进行粘贴操作
    private static void pasteText(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    // 在某个范围内截图
    private static BufferedImage captureScreen(int x, int y, int width, int height) {
        return robot.createScreenCapture(new Rectangle(x, y, width, height));
    }

    // 保存截图
    private static void saveScreenshot(BufferedImage screenshot, File screenshotFile) {
        try {
            ImageIO.write(screenshot, "png", screenshotFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 移动到相应位置并点击鼠标左键
    private static void moveToAndClick(int x, int y) {
        robot.mouseMove(-1, -1);
        robot.mouseMove(x, y);
        clickMouse();
    }

    // 点击鼠标左键
    private static void clickMouse() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }
}

