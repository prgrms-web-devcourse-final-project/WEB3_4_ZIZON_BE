package com.ll.dopdang.global.s3;

import java.net.URL;
import java.time.Duration;

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
 * S3 Presigned URL 생성을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class S3Service {

	private static final String BUCKET_NAME = "devcouse4-team16-bucket";

	/**
	 * 프로젝트 이미지 업로드용 Presigned URL 생성
	 *
	 * @param projectId 프로젝트 ID
	 * @param fileName 저장할 파일 이름 (ex. image_0.png)
	 * @return Presigned URL
	 */
	public URL generatePresignedUrlForProjectImage(Long projectId, String fileName, String contentType) {
		String key = buildProjectImageKey(projectId, fileName);
		return generatePresignedUrl(key, contentType);
	}

	/**
	 * 회원 프로필 이미지 업로드용 Presigned URL 생성
	 *
	 * @param memberId 회원 ID
	 * @param fileName 저장할 파일 이름 (ex. image_0.png)
	 * @return Presigned URL
	 */
	public URL generatePresignedUrlForMemberProfile(Long memberId, String fileName, String contentType) {
		String key = buildMemberProfileKey(memberId, fileName);
		return generatePresignedUrl(key, contentType);
	}

	/**
	 * 지정된 key에 대해 S3에 presigned URL을 생성하는 메서드입니다.
	 *
	 * @param key 업로드할 S3 오브젝트 키 (예: "project-images/uuid.jpg")
	 *        ContentType: 업로드할 이미지 파일의 MIME 타입 (ex. "image/jpeg")
	 * @return 프론트에서 사용할 수 있는 미리 서명된 업로드 URL
	 */
	private URL generatePresignedUrl(String key, String contentType) {
		// try-with-resources 문으로 S3Presigner 객체를 자동으로 닫음
		try (S3Presigner presigner = S3Presigner.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(ProfileCredentialsProvider.create()) // 로컬 AWS 인증 정보(profile 설정) 사용
			.build()) {

			// 미리 서명된 업로드 URL 생성을 위한 요청 객체 생성
			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10)) // URL 유효 기간: 10분
				.putObjectRequest(builder -> builder
					.bucket(BUCKET_NAME) // 대상 S3 버킷
					.key(key) // 업로드할 파일 경로 (ex: "projects/1/image.png")
					.contentType(contentType) // 업로드할 파일의 MIME 타입 (ex: image/png)
					.build())
				.build();

			// presigner를 사용해 실제 presigned URL 생성 후 반환
			return presigner.presignPutObject(presignRequest).url();
		} catch (S3Exception e) {
			throw convertS3Exception(e); // 💡 핵심 개선 포인트
		} catch (SdkClientException e) {
			throw new PresignedUrlException(ErrorCode.AWS_CLIENT_ERROR);
		} catch (IllegalArgumentException e) {
			throw new PresignedUrlException(ErrorCode.INVALID_S3_REQUEST);
		} catch (Exception e) {
			throw new PresignedUrlException(ErrorCode.PRESIGNED_URL_CREATION_FAILED);
		}
	}

	/**
	 * S3Exception을 ErrorCode로 매핑해주는 도우미 메서드
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
	 * S3에 저장할 프로젝트 이미지의 Key(파일 경로)를 생성합니다.
	 * 예시 결과: "projects/42/image1.png"
	 *
	 * @param projectId 프로젝트 ID
	 * @param fileName 저장할 파일 이름
	 * @return S3 업로드용 Key 문자열
	 */
	private String buildProjectImageKey(Long projectId, String fileName) {
		return String.format("projects/%d/%s", projectId, fileName);
	}

	/**
	 * S3에 저장할 회원 프로필 이미지의 Key(파일 경로)를 생성합니다.
	 * 예시 결과: "members/profile/42/profile.png"
	 *
	 * @param memberId 회원 ID
	 * @param fileName 저장할 파일 이름
	 * @return S3 업로드용 Key 문자열
	 */
	private String buildMemberProfileKey(Long memberId, String fileName) {
		return String.format("members/profile/%d/%s", memberId, fileName);
	}

}
