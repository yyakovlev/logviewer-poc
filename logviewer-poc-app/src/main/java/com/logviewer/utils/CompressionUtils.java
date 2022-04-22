package com.logviewer.utils;

import com.logviewer.data2.Log;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;

public class CompressionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CompressionUtils.class);

    public static Path unpackIfRequired(Path file) throws IOException {
        if (file.toString().endsWith(".gz")) {
            String tmpDir = "C:/temp"; //System.getProperty("java.io.tmpdir");
            String unpackedFilename = FilenameUtils.removeExtension(file.getFileName().toString());
            Path unpackedFilePath = Paths.get(tmpDir, "logviewer-tmp-" + unpackedFilename);
            if (!Files.exists(unpackedFilePath)) {
                //TODO check somehow that unpacked file is up-to-date (using index?)
                decompressGzip(file, unpackedFilePath);
            }
            //unpack gzip file to internal directory and use it for reading
            return unpackedFilePath;
        } else {
            return file;
        }
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
