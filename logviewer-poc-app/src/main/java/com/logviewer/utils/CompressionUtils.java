package com.logviewer.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.zip.GZIPInputStream;

public class CompressionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CompressionUtils.class);

    private static final String TMP_FILES_SUBDIR = ".viewer";
    private static final String TMP_FILES_PREFIX = "tmp-logviewer-";
    private static final long TMP_FILES_KEEP_HOURS = 24;

    public static Path unpackIfRequired(Path file) throws IOException {
        if (file.toString().endsWith(".gz")) {
            return unpack(file);
        } else {
            return file;
        }
    }

    private static Path unpack(Path file) throws IOException {
        Path enclosingDirectory = file.getParent();
        Path tmpFilesDir = enclosingDirectory.resolve(TMP_FILES_SUBDIR);//"C:/temp"; //System.getProperty("java.io.tmpdir");
        Files.createDirectories(tmpFilesDir);
        cleanupOldFiles(tmpFilesDir);
        String unpackedFilename = FilenameUtils.removeExtension(file.getFileName().toString());
        Path unpackedFilePath = tmpFilesDir.resolve(TMP_FILES_PREFIX + unpackedFilename);
        if (!Files.exists(unpackedFilePath)) {
            //TODO check somehow that unpacked file is up-to-date (using index?)
            decompressGzip(file, unpackedFilePath);
            //Keep file only till application restart
            unpackedFilePath.toFile().deleteOnExit();
        }
        return unpackedFilePath;
    }

    private static void cleanupOldFiles(Path dir) throws IOException {
        try (DirectoryStream<Path> oldFiles = Files.newDirectoryStream(dir,
                CompressionUtils::isOldFile)) {
            for (Path oldFile : oldFiles) {
                LOG.info("Deleting file: {} of size {} as it's older than {} hours ",
                        oldFile.toAbsolutePath(), Files.size(oldFile), TMP_FILES_KEEP_HOURS);
                Files.delete(oldFile);
            }
        }
    }

    private static boolean isOldFile(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
        FileTime creationFileTime = attributes.creationTime();
        LocalDateTime creationLocalDateTime =  LocalDateTime.ofInstant(creationFileTime.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusHours(TMP_FILES_KEEP_HOURS).isAfter(creationLocalDateTime);
    }

    public static void decompressGzip(Path source, Path target) throws IOException {

        LOG.info("Decompressing {} of size {} to {}...", source, Files.size(source), target);

        try (GZIPInputStream gis = new GZIPInputStream(
                new FileInputStream(source.toFile()));
             FileOutputStream fos = new FileOutputStream(target.toFile())) {

            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        LOG.info("Decompressing to {} completed, size of uncompressed file: {}}",
                target, Files.size(target));
    }


}
