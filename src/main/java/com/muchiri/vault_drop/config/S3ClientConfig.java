package com.muchiri.vault_drop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ClientConfig {

     @Value("${aws.region}")
     private String awsRegion;

     @Value("${aws.secret-key}")
     private String awsSecretKey;

     @Value("${aws.access-key}")
     private String awsAccessKey;

     @Value("${aws.bucket}")
     private String awsBucket;

    @Bean
    public S3Client S3Config() {
        AwsCredentials credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);

        return S3Client.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
    
}
