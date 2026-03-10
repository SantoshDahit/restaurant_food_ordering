package com.restaurant.api.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class FileAttachmentUtil {

    public static String generateUniqueFileNameWithTimeStamp(String originalFileName) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sanitizedFileName = originalFileName.replaceAll("\\s+", "_");
        return timeStamp + "_" + sanitizedFileName;
    }
}
