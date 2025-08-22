package group5.backend.config.s3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region:ap-northeast-2}")
    private String region;
    @Value("${aws.credentials.access-key}")
    private String accessKey;  // AWS S3 액세스 키
    @Value("${aws.credentials.secret-key}")
    private String secretKey;  // AWS S3 비밀 키

    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        // 환경변수로부터 AWS 자격증명을 직접 생성하여 반환
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return StaticCredentialsProvider.create(awsCredentials);
    }


    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public S3Presigner s3Presigner(Region awsRegion, AwsCredentialsProvider provider) {
        return S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(provider)
                .build();
    }

    public String getBucket() {
        return bucket;
    }
}

