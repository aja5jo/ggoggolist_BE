package group5.backend.dto.s3;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PresignRequest {
    @NotBlank
    private String dir;          // "events" | "popups" | "stores"
    @NotNull
    private Integer count;       // 1~N
    @NotBlank
    private String contentType;  // image/jpeg | image/png | image/webp
}


