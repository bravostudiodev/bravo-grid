package com.bravostudiodev.selenium;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_core;
import org.sikuli.api.*;
import org.sikuli.api.robot.desktop.DesktopKeyboard;
import org.sikuli.api.robot.desktop.DesktopMouse;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

/**
 * Created by IgorV on 3.11.2016.
 */
public class SikuliScreen {
    private static final Logger LOGGER = Logger.getLogger(SikuliScreen.class.getName());

    private final HashMap<String, ImageTarget> images = new HashMap<>();

    public SikuliScreen() {

    }

    private static String objectSerialize(Object obj) throws IOException {
        try (final ByteArrayOutputStream byout = new ByteArrayOutputStream();
             final ObjectOutputStream out = new ObjectOutputStream(byout)
        ) {
            out.writeObject(obj);
            LOGGER.info("Serialized data");
            return DatatypeConverter.printBase64Binary(byout.toByteArray());
        }
    }

    private static Object objectParse(String objStr) throws IOException, ClassNotFoundException {
        byte[] objBytes = DatatypeConverter.parseBase64Binary(objStr);

        try (final ByteArrayInputStream byin = new ByteArrayInputStream(objBytes);
             final ObjectInputStream in = new ObjectInputStream(byin)
        ) {
            LOGGER.info("Parsing data...");
            return in.readObject();
        }
    }

    public static ScreenRegion getScreenRegion(int x, int y, int w, int h) {
        ScreenRegion primary = new DesktopScreenRegion();
        return primary.getRelativeScreenRegion(x, y, w, h);
    }

    private static ScreenRegion getScreenRegion(java.awt.Rectangle rc) {
        return getScreenRegion((int) rc.getX(), (int) rc.getY(), (int) rc.getWidth(), (int) rc.getHeight());
    }

    public static java.awt.Rectangle getRectangle(ScreenRegion rgn) {
        return rgn.getBounds();
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
        LOGGER.info("taking screenshot");
        return imageToBase64(new DesktopScreenRegion().capture());
    }

    @SuppressWarnings("unused")
    public String takeBase64Screenshot(java.awt.Rectangle rc) throws IOException {
        ScreenRegion region = getScreenRegion(rc);
        LOGGER.info("taking region screenshot");
        return imageToBase64(region.capture());
    }

    public void drag() {
        new DesktopMouse().drag(new DesktopScreenRegion().getCenter());
    }

    public void drag(int xOff, int yOff) {
        new DesktopMouse().drag(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void drag(ScreenRegion region) {
        new DesktopMouse().drag(region.getCenter());
    }

    public void drag(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().drag(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void drop() {
        new DesktopMouse().click(new DesktopScreenRegion().getCenter());
    }

    public void drop(int xOff, int yOff) {
        new DesktopMouse().drop(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void drop(ScreenRegion region) {
        new DesktopMouse().drop(region.getCenter());
    }

    public void drop(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().drop(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void click() {
        new DesktopMouse().click(new DesktopScreenRegion().getCenter());
    }

    public void click(int xOff, int yOff) {
        new DesktopMouse().click(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void click(ScreenRegion region) {
        ScreenLocation loc = region.getCenter();
        LOGGER.info("clicking region");
        new DesktopMouse().click(loc);
    }

    public void click(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().click(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void rightClick() {
        new DesktopMouse().rightClick(new DesktopScreenRegion().getCenter());
    }

    public void rightClick(int xOff, int yOff) {
        new DesktopMouse().rightClick(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void rightClick(ScreenRegion region) {
        new DesktopMouse().rightClick(region.getCenter());
    }

    public void rightClick(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().rightClick(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void doubleClick() {
        new DesktopMouse().doubleClick(new DesktopScreenRegion().getCenter());
    }

    public void doubleClick(int xOff, int yOff) {
        new DesktopMouse().doubleClick(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void doubleClick(ScreenRegion region) {
        new DesktopMouse().doubleClick(region.getCenter());
    }

    public void doubleClick(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().doubleClick(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void hover() {
        new DesktopMouse().hover(new DesktopScreenRegion().getCenter());
    }

    @SuppressWarnings("unused")
    public void hover(int xOff, int yOff) {
        new DesktopMouse().hover(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void hover(ScreenRegion region) {
        new DesktopMouse().hover(region.getCenter());
    }

    @SuppressWarnings("unused")
    public void hover(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().hover(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void move() {
        new DesktopMouse().move(new DesktopScreenRegion().getCenter());
    }

    public void move(int xOff, int yOff) {
        new DesktopMouse().move(new DesktopScreenRegion().getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void move(ScreenRegion region) {
        new DesktopMouse().move(region.getCenter());
    }

    public void move(ScreenRegion region, int xOff, int yOff) {
        new DesktopMouse().move(region.getCenter().getRelativeScreenLocation(xOff, yOff));
    }

    public void press() {
        new DesktopMouse().press();
    }

    public void rightPress() {
        new DesktopMouse().rightPress();
    }

    public void release() {
        new DesktopMouse().release();
    }

    public void rightRelease() {
        new DesktopMouse().rightRelease();
    }

    public void wheel(int direction, int steps) {
        new DesktopMouse().wheel(direction, steps);
    }

    public void mouseDown(int buttons){
        new DesktopMouse().mouseDown(buttons);
    }

    public void mouseUp(){
        new DesktopMouse().mouseUp();
    }

    public void mouseUp(int buttons){
        new DesktopMouse().mouseUp(buttons);
    }

    public ScreenLocation getMouseLocation() {
        return new DesktopMouse().getLocation();
    }

    public void type(String text) {
        new DesktopKeyboard().type(text);
    }

    public void keyDown(int keycode) {
        new DesktopKeyboard().keyDown(keycode);
    }

    public void keyUp(int keycode) {
        new DesktopKeyboard().keyUp(keycode);
    }

    public void keyDown(String keys) {
        new DesktopKeyboard().keyDown(keys);
    }

    public void keyUp() {
        new DesktopKeyboard().keyUp();
    }

    public void keyUp(String keys) {
        new DesktopKeyboard().keyUp(keys);
    }

    public String copy() {
        return new DesktopKeyboard().copy();
    }

    public void paste(String text) {
        new DesktopKeyboard().paste(text);
    }

    public boolean addTarget(String targetName, String b64PNG) throws IOException {
        LOGGER.info("adding target " + targetName);
        ImageTarget targetImg = images.get(targetName);
        if (null == targetImg) {
            ImageTarget targetImg1 = new ImageTarget(imageFromBase64(b64PNG));
            //The basic function used by Sikuli is OpenCV's matchTemplate() feature. The images are internally converted to the RGB color model
            // ignoring alpha channel. Meaning of similarity (between 0.0 and 1.0) is that some differences in pixel intensity/color are
            // compensated, but below 0.8-0.9 might lead to false positives.
            targetImg1.setMinScore(0.8);
            images.put(targetName, targetImg1);
        }
        return true;
    }

    public static class UnknownImageTarget extends RuntimeException {
        public UnknownImageTarget(String msg) {
            super(msg);
        }
    }

//    public ScreenRegion find(String targetName) {
//        LOGGER.info("finding target " + targetName);
//        ImageTarget targetImg = images.get(targetName);
//        if (null == targetImg)
//            throw new UnknownImageTarget("Unkown target name " + targetName + ", not added with addTarget method");
//        try {
//            return new DesktopScreenRegion().find(targetImg);
//        } catch (Exception e) {
//            LOGGER.log(Level.WARNING, "Didn't find image " + targetName);
//            throw e;
//        }
//    }
//
//    public List<ScreenRegion> findAll(String targetName) {
//        LOGGER.info("finding all targets " + targetName);
//        ImageTarget targetImg = images.get(targetName);
//        if (null == targetImg)
//            throw new UnknownImageTarget("Unkown target name " + targetName + ", not added with addTarget method");
//        try {
//            return new DesktopScreenRegion().findAll(targetImg);
//        } catch (Exception e) {
//            LOGGER.log(Level.WARNING, "Didn't find image " + targetName);
//            throw e;
//        }
//    }

    private static byte[] getPngBytes(BufferedImage biImg) throws IOException {
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(biImg, "png", os);
            return os.toByteArray();
        }
    }

    public static ArrayList<java.awt.Rectangle> findAll(BufferedImage biFull, BufferedImage biTpl, double MIN_SCORE, java.awt.Dimension fullDim) throws IOException {
        assert 0.0 <= MIN_SCORE && MIN_SCORE <= 1.0;

        // new Integer(b64PNG.hashCode()).toString();

        byte[] byFull = getPngBytes(biFull);
        byte[] byTpl = getPngBytes(biTpl);
//        Mat imgSrcColor = imread(pathImage);
//        Mat imgSrcGray = new Mat(imgSrcColor.size(), CV_8UC1);
//        cvtColor(imgSrcColor, imgSrcGray, COLOR_BGR2GRAY);
        Mat imgSrcGray = imdecode(new Mat(byFull), CV_LOAD_IMAGE_GRAYSCALE);
        Mat imgTplGray = imdecode(new Mat(byTpl), CV_LOAD_IMAGE_GRAYSCALE);
        Size size = new Size(imgSrcGray.cols() - imgTplGray.cols() + 1, imgSrcGray.rows() - imgTplGray.rows() + 1);
        Mat imgResult = new Mat(size, CV_32FC1);
        matchTemplate(imgSrcGray, imgTplGray, imgResult, CV_TM_CCOEFF_NORMED/*TM_CCORR_NORMED*/);
        threshold(imgResult, imgResult, MIN_SCORE, 1.0, CV_THRESH_TOZERO);

        ArrayList<java.awt.Rectangle> lMatches = new ArrayList<>();
        while (true) {
            DoublePointer minVal = new DoublePointer(1), maxVal = new DoublePointer(1);
            opencv_core.Point minLoc = new opencv_core.Point(), maxLoc = new opencv_core.Point();
            minMaxLoc(imgResult, minVal, maxVal, minLoc, maxLoc, null);

            opencv_core.Point matchLoc = maxLoc;
            if (maxVal.get() < MIN_SCORE)
                break;

            java.awt.Point pt = new java.awt.Point(matchLoc.x(), matchLoc.y());
            java.awt.Dimension dim = new java.awt.Dimension(imgTplGray.cols(), imgTplGray.rows());
            lMatches.add(new java.awt.Rectangle(pt, dim));
            floodFill(imgResult, matchLoc, new Scalar(0), null, new Scalar(0.1), new Scalar(1.0), 0); // eliminate for next minMaxLoc call
        }
        if(fullDim != null) {
            fullDim.setSize(new java.awt.Dimension(imgSrcGray.cols(), imgSrcGray.rows()));
        }
        return lMatches;
    }

    public static java.awt.Rectangle findMostSimilar(BufferedImage biFull, BufferedImage biTpl, double MIN_SCORE, java.awt.Dimension fullDim) throws IOException {
        ArrayList<java.awt.Rectangle> lMatches = findAll(biFull, biTpl, MIN_SCORE, fullDim);
        return lMatches.isEmpty() ? null : lMatches.get(0); // first match has highest similarity
    }

    public String findAllOnScreen(String targetName, double MIN_SCORE, java.awt.Dimension fullDim) throws IOException {
        LOGGER.info("finding target " + targetName);
        ImageTarget targetImg = images.get(targetName);
        if (null == targetImg)
            throw new UnknownImageTarget("Unkown target name " + targetName + ", not added with addTarget method");
        ScreenRegion primary = new DesktopScreenRegion();

        return objectSerialize(findAll(primary.capture(), targetImg.getImage(), MIN_SCORE, fullDim));
    }
}
