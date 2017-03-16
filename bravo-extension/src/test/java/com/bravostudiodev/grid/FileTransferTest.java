package com.bravostudiodev.grid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by IgorV on 13.2.2017.
 */
public class FileTransferTest {
    private static final String EXPECTED_CONTENT = "expected content";

    private File fileToGet;
    private String fileUploaded = null;
    private String dirUploaded = null;

    @Before
    public void setUp() throws Exception {
        fileToGet = File.createTempFile("test", ".txt");
        FileUtils.write(fileToGet, EXPECTED_CONTENT, StandardCharsets.UTF_8);
    }

    @After
    public void tearDown() throws Exception {
        assertTrue(fileToGet.delete());
        if(null != fileUploaded)
            assertTrue(new File(fileUploaded).delete());
        if(null != dirUploaded)
            FileUtils.deleteDirectory(new File(dirUploaded));
    }

    @Test
    public void canDownloadFile() throws IOException {
        FileTransfer files = new FileTransfer();
        String fileDataB64 = files.readFile(fileToGet.getAbsolutePath());
        String fileStr = new String(DatatypeConverter.parseBase64Binary(fileDataB64), StandardCharsets.UTF_8);
        assertThat(fileStr, is(EXPECTED_CONTENT));
    }

    @Test
    public void canUploadFile() throws IOException {
        FileTransfer files = new FileTransfer();
        fileUploaded = files.saveFile(".txt", DatatypeConverter.printBase64Binary(EXPECTED_CONTENT.getBytes(StandardCharsets.UTF_8)));
        assertThat(fileUploaded, is(notNullValue()));

        String fileDataB64 = files.readFile(fileUploaded);
        String fileStr = new String(DatatypeConverter.parseBase64Binary(fileDataB64), StandardCharsets.UTF_8);
        assertThat(fileStr, is(EXPECTED_CONTENT));
    }

    private static void zipItem(Path topPath, Path currentSubPath, ZipOutputStream zos) throws IOException {
        Path currentPath = topPath.resolve(currentSubPath);
        String entryName = currentSubPath.toString().replaceAll("\\\\", "/");
        File currentFile = currentPath.toFile();
        if(currentFile.isDirectory()) {
            ZipEntry ze = new ZipEntry(entryName + "/"); // this only allows empty directories to be zipped, otherwise it could be skipped
            zos.putNextEntry(ze);
            File[] zipFiles = currentFile.listFiles();
            if(null != zipFiles) {
                for (File file : zipFiles)
                    zipItem(topPath, topPath.relativize(file.toPath()), zos);
            }
        } else {
            ZipEntry ze = new ZipEntry(entryName);
            ze.setSize(currentFile.length());
            ze.setTime(currentFile.lastModified());
            zos.putNextEntry(ze);
            try (FileInputStream in = new FileInputStream(currentFile.getAbsoluteFile())) {
                IOUtils.copy(in, zos);
            }
        }
    }

    public static void zipIt(String inputFolder, OutputStream osZip) throws IOException {
        Path inputPath = Paths.get(inputFolder).toAbsolutePath();
        Path topPath = inputPath.getParent().toAbsolutePath();
        Path currentPath = topPath.relativize(inputPath);

        try (ZipOutputStream zos = new ZipOutputStream(osZip)) {
            zipItem(topPath, currentPath, zos);
            zos.closeEntry();
        }
    }

    @Test
    public void canUploadZip() throws URISyntaxException, IOException {
        File currentClassURI = new File(FileTransferTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String uploadDir = new File(currentClassURI, "upload").getAbsolutePath();
        ByteArrayOutputStream osBytes = new ByteArrayOutputStream();
        zipIt(uploadDir, osBytes);

        FileTransfer files = new FileTransfer();
        dirUploaded = files.saveZipContent(DatatypeConverter.printBase64Binary(osBytes.toByteArray()));
        assertThat(dirUploaded, is(notNullValue()));

        verifyFilesInZip(new File(dirUploaded),
            "upload",
            "upload/first.txt",
            "upload/directory",
            "upload/directory/second.txt",
            "upload/directory/dir",
            "upload/directory/dir/third.txt");
    }

    private void verifyFilesInZip(File dir, String... paths) {
        for (String path : paths) {
            String failMsg = String.format("File %s not exists in dir: %s", path, dir.getAbsolutePath());
            assertTrue(failMsg, new File(dir, path).exists());
        }
    }
}
