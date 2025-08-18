package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.HomeFeedService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeFeedController {

    private final HomeFeedService homeFeedService;

    @Operation(summary = "홈 미리보기(6개)", description = "가게/이벤트/팝업 섞어서 likeCount desc, id desc로 6개 반환. 로그인 시 liked 반영")
    @GetMapping("/home")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<CategoryFeedItemResponse>>> homePreview(
            @AuthenticationPrincipal User loginUser
    ) {
        Long userId = (loginUser != null) ? loginUser.getId() : null;
        var data = homeFeedService.getHomePreview(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "홈 미리보기 조회 성공", data));
    }

    @Operation(summary = "홈 더보기(18개)", description = "가게/이벤트/팝업 섞어서 likeCount desc, id desc로 18개 반환. 로그인 시 liked 반영")
    @GetMapping("/home/detail")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<CategoryFeedItemResponse>>> homeDetail(
            @AuthenticationPrincipal User loginUser
    ) {
        Long userId = (loginUser != null) ? loginUser.getId() : null;
        var data = homeFeedService.getHomeDetail(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "홈 더보기 조회 성공", data));
    }
}
