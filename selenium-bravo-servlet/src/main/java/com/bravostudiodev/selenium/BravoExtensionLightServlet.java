package com.bravostudiodev.selenium;

import com.google.gson.Gson;
import io.sterodium.rmi.protocol.MethodInvocationDto;
import io.sterodium.rmi.protocol.MethodInvocationResultDto;
import io.sterodium.rmi.protocol.server.RmiFacade;
import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.robot.desktop.DesktopKeyboard;
import org.sikuli.api.robot.desktop.DesktopMouse;
import org.sikuli.api.robot.desktop.DesktopScreen;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
public class BravoExtensionLightServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BravoExtensionLightServlet.class.getName());

    private static final Gson GSON = new Gson();

    private final RmiFacade rmiFacade;

    private static void AddToRmiFacade(RmiFacade rmif, String objId, Object obj, String objDescription) {
        try {
            rmif.add(objId, obj);
        } catch (ExceptionInInitializerError e) {
            LOGGER.log(Level.SEVERE, objDescription + " operations are not available on this environment.", e);
            throw e;
        }
    }
    public BravoExtensionLightServlet() {
        super();

        rmiFacade = new RmiFacade();
        AddToRmiFacade(rmiFacade, "screen", new SikuliScreen(), "Sikuli");
        DesktopScreenRegion desktopScreenRegion = new DesktopScreenRegion();
        DesktopScreen desktopScreen = (DesktopScreen) desktopScreenRegion.getScreen();
        AddToRmiFacade(rmiFacade, "primary_screen_region", new DesktopScreenRegion(), "Sikuli desktop region");
        AddToRmiFacade(rmiFacade, "primary_screen", desktopScreen, "DeskSikuli desktop");
        AddToRmiFacade(rmiFacade, "mouse", new DesktopMouse(), "Mouse pointer");
        AddToRmiFacade(rmiFacade, "keyboard", new DesktopKeyboard(), "Keyboard");
        AddToRmiFacade(rmiFacade, "files", new FileTransfer(), "File transfer");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String objectId = getObjectId(req);
        if (objectId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't find object ID in URL string");
            return;
        }
        MethodInvocationDto method = GSON.fromJson(req.getReader(), MethodInvocationDto.class);
        MethodInvocationResultDto result = rmiFacade.invoke(objectId, method);
        resp.getWriter().write(GSON.toJson(result));
    }

    private String getObjectId(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        Pattern pattern = Pattern.compile(".+/([^/]+)");
        Matcher matcher = pattern.matcher(requestURI);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }
}
