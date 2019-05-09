package com.example.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

@Configuration
public class AmazonS3Config 
{
	@Value("#{environment.AWS_ACCESS_KEY_ID}")
    private String awsKeyId;

	@Value("#{environment.AWS_SECRET_ACCESS_KEY}")
    private String awsKeySecret;

	@Value("#{environment.AWS_REGION}")
    private String awsRegion;

	@Value("#{environment.AWS_S3_BUCKET_NAME}")
    private String awsBucket;

    @Bean(name = "awsKeyId")
    public String getAWSKeyId() {
        return awsKeyId;
    }

    @Bean(name = "awsKeySecret")
    public String getAWSKeySecret() {
        return awsKeySecret;
    }

    @Bean(name = "awsRegion")
    public Region getAWSRegion() {
        return Region.getRegion(Regions.fromName(awsRegion));
    }

    @Bean(name = "awsCredentialsProvider")
    public AWSCredentialsProvider getAWSCredentials() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(this.awsKeyId, this.awsKeySecret);
        return new AWSStaticCredentialsProvider(awsCredentials);
    }

    @Bean(name = "awsBucket")
    public String getAwsBucket() {
        return awsBucket;
    }
}
