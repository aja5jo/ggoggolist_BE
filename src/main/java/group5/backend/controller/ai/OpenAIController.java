package group5.backend.controller.ai;

import group5.backend.dto.chat.ChatRequest;
import group5.backend.dto.chat.ChatResponse;
import group5.backend.service.ai.OpenAIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class OpenAIController {
    private final OpenAIChatService openAiChatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request){
        String response=openAiChatService.getChatResponse(request);
        return ResponseEntity.ok(new ChatResponse(response));
    }

}
