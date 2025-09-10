package com.example.demo.util;


import io.micrometer.common.util.StringUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;

@Slf4j
@NoArgsConstructor
public class FileUtil {

    public static byte[] readFileFromLocation(String fileUrl) {
        if (StringUtils.isBlank(fileUrl)) {
            return new byte[0];
        }
        try {
            return Files.readAllBytes(new File(fileUrl).toPath());
        } catch (java.io.IOException e) {
            log.error("Failed to read file from location: {}", fileUrl, e);
        }
        return new byte[0];
    }

}
