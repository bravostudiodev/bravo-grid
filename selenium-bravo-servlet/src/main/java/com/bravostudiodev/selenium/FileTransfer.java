package com.bravostudiodev.selenium;

import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Paths;
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

    public String saveFile(String fullAbsPathOrPrefixForTempName, String fileB64Data) throws IOException {
        LOGGER.info("Saving file " + fullAbsPathOrPrefixForTempName);
        File targetFile = (Paths.get(fullAbsPathOrPrefixForTempName).isAbsolute())
                ? new File(fullAbsPathOrPrefixForTempName)
                : File.createTempFile("upload", fullAbsPathOrPrefixForTempName);
        byte[] data = DatatypeConverter.parseBase64Binary(fileB64Data);
        try (InputStream isBytes = new ByteArrayInputStream(data);
            OutputStream osFile = new FileOutputStream(targetFile)) {
            LOGGER.info("Copying input data to file");
            IOUtils.copy(isBytes, osFile);
        }
        return targetFile.getAbsolutePath();
    }

    public String saveZipContent(String b64Zip, String wantedTargetDir) throws IOException {
        byte[] zipBytes = DatatypeConverter.parseBase64Binary(b64Zip);
        File outputFolder = (wantedTargetDir == null)
                ? Files.createTempDir()
                : new File(wantedTargetDir);

        LOGGER.info("Unzipping archive");
        unZipIt(new ByteArrayInputStream(zipBytes), outputFolder);
        return outputFolder.getAbsolutePath();
    }

    public String saveZipContent(String b64Zip) throws IOException {
        return saveZipContent(b64Zip, null);
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
