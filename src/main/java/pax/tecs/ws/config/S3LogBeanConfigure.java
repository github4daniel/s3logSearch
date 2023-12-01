package pax.tecs.ws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

@Configuration
public class S3LogBeanConfigure {

	@Value("${S3_ACCESS_KEY}")
	private String awsAccessKey;

	@Value("${S3_SECRET_KEY}")
	private String awsSecreteKey;

	@Bean
	public AmazonS3 s3Client() {
		AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(awsAccessKey, awsSecreteKey));

		return AmazonS3Client.builder().withRegion(Regions.US_EAST_1).withCredentials(provider).build();
	}

}
