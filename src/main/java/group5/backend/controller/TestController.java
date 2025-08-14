package group5.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name="테스트용 컨트롤러")
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public String hello() {
        return "Salut, Swagger!";
    }
}
