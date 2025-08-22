package group5.backend.controller;


import group5.backend.dto.s3.PresignRequest;
import group5.backend.dto.s3.PresignedDto;
import group5.backend.response.ApiResponse;
import group5.backend.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload")
//@PreAuthorize("hasAuthority('MERCHANT')")
public class S3Controller {

    private final S3Service presignService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<PresignedDto>>> presign(@RequestBody @Valid PresignRequest req) {
        var list = presignService.createUploadUrls(req.getDir(), req.getCount(), req.getContentType());
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "presigned ok", list));
    }
}

