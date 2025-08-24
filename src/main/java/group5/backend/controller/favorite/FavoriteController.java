package group5.backend.controller.favorite;
import group5.backend.domain.user.User;
import group5.backend.dto.response.FavoriteResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.favorite.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "유저: 즐겨찾기 설정 및 해제", description = "가게, 이벤트, 팝업 즐겨찾기 설정/해제")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 가게 즐겨찾기 토글
    @Secured("USER") // USER 권한을 가진 사용자만 접근 가능
    @PostMapping("/stores/{storeId}/favorites")
    @Operation(summary = "Toggle store favorite", description = "Toggle the favorite status of a store for a user.")
    public ResponseEntity<ApiResponse<FavoriteResponse>> toggleStoreFavorite(
            @AuthenticationPrincipal User loginUser,
            @PathVariable Long storeId
    ) {
        FavoriteResponse response = favoriteService.toggleStoreFavorite(loginUser, storeId);

        ApiResponse<FavoriteResponse> apiResponse = new ApiResponse<>(
                true,
                200,  // HTTP Status OK
                "가게 즐겨찾기 토글 성공",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    // 이벤트 즐겨찾기 토글
    @Secured("USER")  // USER 권한을 가진 사용자만 접근 가능
    @PostMapping("/events/{eventId}/favorites")
    @Operation(summary = "Toggle event favorite", description = "Toggle the favorite status of an event for a user.")
    public ResponseEntity<ApiResponse<FavoriteResponse>> toggleEventFavorite(
            @AuthenticationPrincipal User loginUser,
            @PathVariable Long eventId
    ) {
        FavoriteResponse response = favoriteService.toggleEventFavorite(loginUser, eventId);

        ApiResponse<FavoriteResponse> apiResponse = new ApiResponse<>(
                true,
                200,  // HTTP Status OK
                "이벤트 즐겨찾기 토글 성공",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    // 팝업 즐겨찾기 토글
    @Secured("USER")  // USER 권한을 가진 사용자만 접근 가능
    @PostMapping("/popups/{popupId}/favorites")
    @Operation(summary = "Toggle popup favorite", description = "Toggle the favorite status of a popup for a user.")
    public ResponseEntity<ApiResponse<FavoriteResponse>> togglePopupFavorite(
            @AuthenticationPrincipal User loginUser,
            @PathVariable Long popupId
    ) {
        FavoriteResponse response = favoriteService.togglePopupFavorite(loginUser, popupId);

        ApiResponse<FavoriteResponse> apiResponse = new ApiResponse<>(
                true,
                200,  // HTTP Status OK
                "팝업 즐겨찾기 토글 성공",
                response
        );

        return ResponseEntity.ok(apiResponse);
    }
    // 유저의 전체 즐겨찾기 목록 조회
    @Secured("USER")  // USER 권한을 가진 사용자만 접근 가능
    @GetMapping("/favorites")
    @Operation(summary = "Get all favorites", description = "Get all the favorite stores, events, and popups of a user.")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getAllFavorites(
            @AuthenticationPrincipal User loginUser
    ) {
        log.info("유저 ID: {} 의 전체 즐겨찾기 목록 조회 요청", loginUser.getId());

        List<FavoriteResponse> response = favoriteService.getAllFavorites(loginUser);

        log.info("유저 ID: {} 의 전체 즐겨찾기 목록 조회 완료", loginUser.getId());

        ApiResponse<List<FavoriteResponse>> apiResponse = new ApiResponse<>(
                true,
                200,  // HTTP Status OK
                "전체 즐겨찾기 목록 조회 성공",
                response
        );
        log.info("전체 즐겨찾기 목록 조회 응답: {}", apiResponse);
        return ResponseEntity.ok(apiResponse);
    }
}