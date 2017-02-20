package com.bravostudiodev.grid;

import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.ImageTarget;
import org.sikuli.api.ScreenRegion;
import org.sikuli.api.robot.Keyboard;
import org.sikuli.api.robot.Mouse;
import org.sikuli.api.robot.desktop.DesktopKeyboard;
import org.sikuli.api.robot.desktop.DesktopMouse;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IgorV on 3.11.2016.
 */
public class SikuliScreen {
    private static final Logger LOGGER = Logger.getLogger(SikuliScreen.class.getName());

    private final ScreenRegion primaryScreen;
    private final Mouse remoteMouse;
    private final Keyboard remoteKeyboard;
    private final HashMap<String, ImageTarget> images = new HashMap<>();

    public SikuliScreen() {
        primaryScreen = new DesktopScreenRegion();
        remoteMouse = new DesktopMouse();
        remoteKeyboard = new DesktopKeyboard();
    }

    private static String imageToBase64(BufferedImage bi) throws IOException {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(bi, "png", os);
            return DatatypeConverter.printBase64Binary(os.toByteArray());
        }
    }

    private static BufferedImage imageFromBase64(String b64PNG) throws IOException {
        byte[] imgBytes = DatatypeConverter.parseBase64Binary(b64PNG);
        try (InputStream isBytes = new ByteArrayInputStream(imgBytes)) {
            return ImageIO.read(isBytes);
        }
    }

    public String takeBase64Screenshot() throws IOException {
        return imageToBase64(primaryScreen.capture());
    }

    public String takeBase64Screenshot(ScreenRegion region) throws IOException {
        return imageToBase64(region.capture());
    }

    public void click() {
        remoteMouse.click(primaryScreen.getCenter());
    }

    public void click(int xOff, int yOff) {
        remoteMouse.click(primaryScreen.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void click(ScreenRegion region) {
        remoteMouse.click(region.getCenter());
    }

    public void click(ScreenRegion region, int xOff, int yOff) {
        remoteMouse.click(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void rightClick() {
        remoteMouse.rightClick(primaryScreen.getCenter());
    }

    public void rightClick(int xOff, int yOff) {
        remoteMouse.rightClick(primaryScreen.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void rightClick(ScreenRegion region) {
        remoteMouse.rightClick(region.getCenter());
    }

    public void rightClick(ScreenRegion region, int xOff, int yOff) {
        remoteMouse.rightClick(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void type(String text) {
        remoteKeyboard.type(text);
    }

    public void keyDown(int keycode) {
        remoteKeyboard.keyDown(keycode);
    }

    public void keyUp(int keycode) {
        remoteKeyboard.keyUp(keycode);
    }

    public void keyDown(String keys) {
        remoteKeyboard.keyDown(keys);
    }

    public void keyUp() {
        remoteKeyboard.keyUp();
    }

    public void keyUp(String keys) {
        remoteKeyboard.keyUp(keys);
    }

    public String copy() {
        return remoteKeyboard.copy();
    }

    public void paste(String text) {
        remoteKeyboard.paste(text);
    }

    public boolean addTarget(String targetName, String b64PNG) throws IOException {
        ImageTarget targetImg = images.get(targetName);
        if (null == targetImg) {
            ImageTarget targetImg1 = new ImageTarget(imageFromBase64(b64PNG));
            targetImg1.setMinScore(0.8);
            images.put(targetName, targetImg1);
            targetImg = targetImg1;
        }
        return (null != targetImg);
    }

    public ScreenRegion find(String targetName) {
        ImageTarget targetImg = images.get(targetName);
        if (null == targetImg)
            throw new RuntimeException("Unkown target name " + targetName + ", not added with addTarget method");
        try {
            return primaryScreen.find(targetImg);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Didn't find image " + targetName);
            throw e;
        }
    }

    public List<ScreenRegion> findAll(String targetName) {
        ImageTarget targetImg = images.get(targetName);
        if (null == targetImg)
            throw new RuntimeException("Unkown target name " + targetName + ", not added with addTarget method");
        try {
            return primaryScreen.findAll(targetImg);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Didn't find image " + targetName);
            throw e;
        }
    }
}
