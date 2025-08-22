package group5.backend.service.ai.recomm;

public interface UserProfileService {
    float[] getOrBuild(Long userId);
    void invalidate(Long userId);
}
