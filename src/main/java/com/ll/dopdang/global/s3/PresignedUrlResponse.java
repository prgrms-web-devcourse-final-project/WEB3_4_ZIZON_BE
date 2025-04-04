package com.ll.dopdang.global.s3;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "S3 Presigned URL 응답")
public record PresignedUrlResponse(

	@Schema(description = "S3에 파일을 업로드할 수 있는 미리 서명된 URL", example = "https://bucket-name.s3.ap-northeast-2.amazonaws.com/path/to/file?X-Amz-Algorithm=...")
	String presignedUrl

) {
}
