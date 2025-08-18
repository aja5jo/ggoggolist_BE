package group5.backend.config.openai;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    // 환경변수에서 OPENAI_API_KEY 값을 읽어옵니다.
    @Value("${OPENAI_API_KEY}")
    private String openaikey;

    @Bean
    public OpenAiService openAiService(){
        return new OpenAiService(openaikey);
    }
}

