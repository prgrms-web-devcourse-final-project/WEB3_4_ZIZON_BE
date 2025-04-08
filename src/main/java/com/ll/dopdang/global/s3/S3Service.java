package com.ll.dopdang.global.s3;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.PresignedUrlException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * S3 Presigned URL 생성을 담당하는 서비스 클래스입니다.
 * - 업로드용 Presigned URL과, 접근 가능한 S3 URL을 함께 반환합니다.
 * - UUID 기반 파일명으로 경로 충돌을 방지하며, 폴더 단위로 구분 가능합니다.
 */
@Service
@RequiredArgsConstructor
public class S3Service {

	private static final String BUCKET_NAME = "devcouse4-team16-bucket"; // S3 버킷 이름
	private final S3Presigner presigner = S3Presigner.builder()
		.region(Region.AP_NORTHEAST_2)
		.credentialsProvider(ProfileCredentialsProvider.create()) // 또는 EnvironmentVariableCredentialsProvider
		.build();

	/**
	 * Presigned PUT URL을 생성하고, 업로드 후 접근 가능한 S3 URL도 함께 반환합니다.
	 * - UUID는 중복 방지를 위해 사용되며, 폴더 이름에 따라 저장 위치가 구분됩니다.
	 * - 접근 URL은 공개 폴더인 경우에만 외부에서 직접 접근 가능합니다.
	 *
	 * @param request Presigned URL 생성을 위한 요청 정보 (폴더, 파일명, MIME 타입 포함)
	 * @return 업로드용 Presigned URL과 접근 가능한 S3 URL을 포함한 응답 객체
	 */
	public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
		String folder = request.getFolder();
		String fileName = request.getFileName();
		String contentType = request.getContentType();

		// UUID로 중복 방지 → S3 키 구성
		String uuid = UUID.randomUUID().toString();
		String key = String.format("%s/%s_%s", folder, uuid, fileName);

		// 업로드용 presigned URL 생성
		URL presignedUrl = generatePresignedUrlInternal(key, contentType);

		// 퍼블릭 URL (private 파일의 경우 접근 안 될 수 있음)
		String accessUrl = "https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/" + key;

		return new PresignedUrlResponse(presignedUrl.toString(), accessUrl);
	}

	/**
	 * S3에 파일을 업로드할 수 있도록 10분 유효한 Presigned PUT URL을 생성합니다.
	 *폴더 경로를 기준으로 공개 폴더인 경우, ACL을 public-read로 설정합니다.
	 *
	 * @param key          저장될 S3 키 (경로 + 파일명)
	 * @param contentType  파일 MIME 타입
	 * @return 업로드용 presigned URL
	 */
	private URL generatePresignedUrlInternal(String key, String contentType) {
		try {
			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))
				.putObjectRequest(b -> b.bucket(BUCKET_NAME)
					.key(key)
					.contentType(contentType))
				.build();

			return presigner.presignPutObject(presignRequest).url();

		} catch (S3Exception e) {
			throw convertS3Exception(e);
		} catch (SdkClientException e) {
			throw new PresignedUrlException(ErrorCode.AWS_CLIENT_ERROR);
		} catch (Exception e) {
			throw new PresignedUrlException(ErrorCode.PRESIGNED_URL_CREATION_FAILED);
		}
	}

	/**
	 * Presigned GET URL을 생성해 파일 다운로드를 허용합니다.
	 * 단, 요청자가 해당 리소스에 접근 가능한지 검사 후 허용합니다.
	 */
	// public URL generateDownloadUrl(String key, Member requester) {
	// 	if (!hasAccessPermission(key, requester)) {
	// 		throw new ServiceException(ErrorCode.S3_FILE_NOT_FOUND); // 접근 불가 시 404
	// 	}
	//
	// 	try (S3Presigner presigner = S3Presigner.builder()
	// 		.region(Region.AP_NORTHEAST_2)
	// 		.credentialsProvider(ProfileCredentialsProvider.create())
	// 		.build()) {
	//
	// 		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
	// 			.signatureDuration(Duration.ofMinutes(5))
	// 			.getObjectRequest(builder -> builder
	// 				.bucket(BUCKET_NAME)
	// 				.key(key)
	// 				.build())
	// 			.build();
	//
	// 		return presigner.presignGetObject(presignRequest).url();
	// 	}
	// }

	/**
	 * AWS SDK의 예외를 내부 정의된 커스텀 예외로 변환합니다.
	 */
	private PresignedUrlException convertS3Exception(S3Exception exception) {
		String code = exception.awsErrorDetails().errorCode();
		return switch (code) {
			case "AccessDenied" -> new PresignedUrlException(ErrorCode.S3_ACCESS_DENIED);
			case "NoSuchBucket" -> new PresignedUrlException(ErrorCode.S3_BUCKET_NOT_FOUND);
			default -> new PresignedUrlException(ErrorCode.INVALID_S3_REQUEST);
		};
	}

	/**
	 * 해당 S3 객체에 요청자가 접근 권한이 있는지 검사합니다.
	 * - 공개 폴더는 누구나 접근 가능
	 * - 디지털 파일은 구매자 또는 판매자만 접근 가능
	 */
	// private boolean hasAccessPermission(String key, Member requester) {
	// 	// // 공개 폴더는 접근 허용
	// 	// if (isPublicFolder(key)) return true;
	// 	//
	// 	// // 디지털 파일은 접근 제한
	// 	// if (key.startsWith("digital/")) {
	// 	// 	Long productId = extractProductIdFromKey(key);
	// 	// 	// return digitalProductService.hasAccess(productId, requester);
	// 	// 	return false; // TODO: 실제 로직 연결 필요
	// 	// }
	//
	// 	// 그 외는 접근 차단
	// 	return false;
	// }

	// /**
	//  * S3 키에서 디지털 상품 ID를 추출합니다.
	//  * 예: "digital/12345/file.zip" → 12345
	//  */
	// private Long extractProductIdFromKey(String key) {
	// 	try {
	// 		String[] parts = key.split("/");
	// 		return Long.parseLong(parts[1]);
	// 	} catch (Exception e) {
	// 		throw new PresignedUrlException(ErrorCode.INVALID_S3_REQUEST);
	// 	}
	// }
}
