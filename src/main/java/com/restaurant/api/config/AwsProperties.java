package com.restaurant.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class AwsProperties {

    private String bucketName;
    private String region;
    private Map<String, String> folderPaths;

    public Region getRegionEnum() {
        return Region.of(region);
    }
}
