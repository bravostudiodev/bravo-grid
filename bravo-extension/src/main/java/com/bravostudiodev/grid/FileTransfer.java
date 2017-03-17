package com.bravostudiodev.grid;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
public class FileTransfer {
    private static final Logger LOGGER = Logger.getLogger(SikuliScreen.class.getName());

    public String readFile(String filePath) throws IOException {
        LOGGER.info("Reading file " + filePath);
        try (InputStream isFile = new FileInputStream(filePath);
            ByteArrayOutputStream osBytes = new ByteArrayOutputStream()) {
            LOGGER.info("Copying file to byte array");
            IOUtils.copy(isFile, osBytes);
            return DatatypeConverter.printBase64Binary(osBytes.toByteArray());
        }
    }

    public String saveFile(String fileExt, String fileB64Data) throws IOException {
        LOGGER.info("Creating temporary file");
        File tempFile = File.createTempFile("upload", fileExt);
        byte[] data = DatatypeConverter.parseBase64Binary(fileB64Data);
        try (InputStream isBytes = new ByteArrayInputStream(data);
            OutputStream osFile = new FileOutputStream(tempFile)) {
            LOGGER.info("Copying input data to file");
            IOUtils.copy(isBytes, osFile);
        }
        return tempFile.getAbsolutePath();
    }

    public String saveZipContent(String b64Zip) throws IOException {
        byte[] zipBytes = DatatypeConverter.parseBase64Binary(b64Zip);
        File outputFolder = Files.createTempDir();
        LOGGER.info("Unzipping archive");
        unZipIt(new ByteArrayInputStream(zipBytes), outputFolder);
        return outputFolder.getAbsolutePath();
    }

    private static void unZipIt(InputStream isZip, File outputFolder) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(isZip)) { //get the zip file content
            ZipEntry ze;
            while(null != (ze = zis.getNextEntry())) { //get the zipped file list entry
                String fileName = ze.getName();
                File outFile = new File(outputFolder, fileName);
                if (ze.isDirectory()) {
                    //noinspection ResultOfMethodCallIgnored
                    outFile.mkdir();
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        IOUtils.copy(zis, fos);
                    }
                }
            }
        }
    }
}
