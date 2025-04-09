package com.ll.dopdang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ActiveProfiles("test")
@SpringBootTest
class DopdangApplicationTests {

	@MockBean
	private S3Presigner s3Presigner;

	@Test
	void contextLoads() {
	}

}
