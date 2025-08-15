package group5.backend.controller;

import group5.backend.dto.common.store.response.StoreEventResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.HomeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService homeQueryService;

    // 세션에 USER_ID 가 있으면 로그인 사용자 로직, 없으면 비로그인 로직으로 동작
    @GetMapping("/api/home")
    public ResponseEntity<ApiResponse<StoreEventResponse>> getHome(
            @SessionAttribute(name = "USER_ID", required = false) Long userId
    ) {
        var data = homeQueryService.getHome(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "메인 페이지 조회 성공", data));
    }
}
