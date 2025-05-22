package com.cpl.reconciliation.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

@Slf4j
public class CPLFileUtils {

    public static boolean isDirectoryWithContent(File folder) {
        return folder != null && folder.exists() && folder.isDirectory() && folder.listFiles() != null;
    }

    public static FileFilter extentionFilter(String extention) {
        return (f) -> f.isFile() && f.getName().endsWith(extention);
    }

    public static void deleteDirectory(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new DeleteFileVisitor());
                log.info("Directory deleted: " + path);
            } else {
                log.info("Directory does not exist: " + path);
            }
        } catch (Exception e) {
            log.error("Exception in deleting directory: ", e);
        }
    }

    static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                throw exc;
            }
        }
    }

}
