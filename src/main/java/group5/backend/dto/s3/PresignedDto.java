package group5.backend.dto.s3;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignedDto {
    private String key;        // S3 object key
    private String uploadUrl;  // PUT presigned URL (5~10분 유효)
    private String publicUrl;  // 최종 DB에 저장하는 공개 URL
}