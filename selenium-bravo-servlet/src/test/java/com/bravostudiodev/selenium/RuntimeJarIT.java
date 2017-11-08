package com.bravostudiodev.selenium;

import com.bravostudiodev.hello.Hello;
import io.sterodium.rmi.protocol.client.RemoteNavigator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
/**
 * Created by IgorV on 13.2.2017.
 */
public class RuntimeJarIT {
    String basePath = "/" + BravoExtensionLightServlet.class.getSimpleName();
    RemoteNavigator navigator;

    @Before
    public void setUp() throws Exception {
        navigator = new RemoteNavigator("192.168.33.10", 4480, basePath + "/" + new Random().longs(10).toString());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void canUploadMainJarFile() throws IOException, URISyntaxException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        //Load main JAR bytes
        File jarPathLocal = new File(Hello.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertThat(jarPathLocal, notNullValue());
        byte[] jarContent = new byte[(int) jarPathLocal.length()];
        try (FileInputStream in = new FileInputStream(jarPathLocal)) {
            in.read(jarContent);
        }

        //Save main JAR bytes to remote
        FileTransfer files = navigator.createProxy(FileTransfer.class, "files");
        String jarPathRemote = files.saveFile(".jar", DatatypeConverter.printBase64Binary(jarContent));
        assertThat(jarPathRemote, is(notNullValue()));

        //Extend remote with new instance of Hello
        RuntimeJar extender = navigator.createProxy(RuntimeJar.class, "remote_extender");
        extender.addRemoteInstance(jarPathRemote, "helloinstance", Hello.class.getName());

        //Get that new remote instance
        Hello remoteHello = navigator.createProxy(Hello.class, "helloinstance");
        assertThat(remoteHello, is(notNullValue()));

        assertThat(remoteHello.sayHello(), equalTo("Hello"));
        assertThat(remoteHello.echo("Hi there!"), equalTo("Hi there!"));
        assertThat(remoteHello.concat("Left", "Right"), equalTo("LeftRight"));
    }
}
