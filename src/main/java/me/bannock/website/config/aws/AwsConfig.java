package me.bannock.website.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsConfig {

    @Value("${bannock.aws.region}")
    private Region region;
    @Value("${bannock.aws.accessKeyId}")
    private String accessKeyId;
    @Value("${bannock.aws.secretAccessKey}")
    private String secretAccessKey;

    @Bean
    public SesClient getSesClient(){
        return SesClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }

}
