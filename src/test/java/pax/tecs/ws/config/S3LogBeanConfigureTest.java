package pax.tecs.ws.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.amazonaws.services.s3.AmazonS3;

@SpringBootTest(classes={S3LogBeanConfigure.class})
class S3LogBeanConfigureTest {
	
	@Autowired
	private S3LogBeanConfigure s3LogBeanConfigure;
	
	
	@BeforeEach
	public void setUp() {
		System.setProperty("S3_ACCESS_KEY", "AKIAQFPFWFJ");
		System.setProperty("S3_SECRET_KEY", "YKI8e3fGX16OtjSuRgEndnjOx");
		
	}
	
	@AfterEach
	public void tearDown() {
		System.clearProperty("S3_ACCESS_KEY");
		System.clearProperty("S3_SECRET_KEY");
		
	}
	
	@Test
	void testS3ClientBuild() {
		AmazonS3  s3Client = s3LogBeanConfigure.s3Client();
		assertNotNull(s3Client);
	}

}
