package com.bravostudiodev.selenium;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class RuntimeJar {
    private static final Logger LOGGER = Logger.getLogger(SikuliScreen.class.getName());

    private BiConsumer<String, Object> addToRmiFacade = null;
    public RuntimeJar(BiConsumer<String, Object> addToRmiFacade) {
        this.addToRmiFacade = addToRmiFacade;
    }

    /* https://stackoverflow.com/questions/27187566/load-jar-dynamically-at-runtime
     * Adds the supplied Java Archive library to java.class.path. This is benign
     * if the library is already loaded.
     */
    private synchronized void loadModule(String jarPath) throws TypeNotPresentException {
        LOGGER.info("Loading module " + jarPath);
        File jar = new File(jarPath);
        try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (URL it : Arrays.asList(loader.getURLs())){
                if (it.equals(url))
                    return;
            }
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
            method.setAccessible(true); /*promote the method to public access*/
            method.invoke(loader, new Object[]{url});
            LOGGER.info("... loaded module " + jarPath);
        } catch (final NoSuchMethodException | IllegalAccessException
                | MalformedURLException | InvocationTargetException e){
            throw new TypeNotPresentException("FAILED loading module ", e);
        }
    }

    public void addRemoteInstance(String jarPath, String remoteId, String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        loadModule(jarPath);
        LOGGER.info("Creating object of class " + className);
        Object obj = Class.forName(className).newInstance();
        LOGGER.info("Registering object " + className);
        addToRmiFacade.accept(remoteId, obj);
    }
}
