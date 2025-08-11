package group5.backend.domain.user;

import group5.backend.domain.event.FavoriteEvent;
import group5.backend.domain.popup.FavoritePopup;
import group5.backend.domain.store.FavoriteStore;
import group5.backend.domain.store.Store;
import group5.backend.exception.category.MerchantInvalidCategorySizeException;
import group5.backend.exception.category.UserInvalidCategorySizeException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_categories",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "category")
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // USER, MERCHANT

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteStore> favoriteStores;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteEvent> favoriteEvents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoritePopup> favoritePopups; // ✅ 새로 추가

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Store store;

    /* -------------------- UserDetails -------------------- */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name())); // "USER" or "MERCHANT"
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /* -------------------- Category Logic -------------------- */

    /** USER 카테고리 토글 */
    public void toggleCategory(Category category) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }

        if (this.categories.contains(category)) {
            if (this.categories.size() == 1) {
                throw new UserInvalidCategorySizeException(
                        "카테고리는 최소 1개 이상 선택해야 합니다.",
                        new ArrayList<>(this.categories)
                );
            }
            this.categories.remove(category); // OFF
        } else {
            if (this.categories.size() >= 3) {
                throw new UserInvalidCategorySizeException(
                        "카테고리는 최대 3개까지 선택할 수 있습니다.",
                        new ArrayList<>(this.categories)
                );
            }
            this.categories.add(category);
        }
    }

    /** MERCHANT 카테고리 설정 (단일) */
    public void setMerchantCategory(Category category) {
        if (category == null) {
            throw new MerchantInvalidCategorySizeException("가게 카테고리는 반드시 1개의 카테고리를 설정해야 합니다.");
        }
        this.categories = List.of(category); // 무조건 1개
    }

    /* -------------------- Helper Methods for Service -------------------- */

    /** MERCHANT 여부 */
    public boolean isMerchant() {
        return this.role == Role.MERCHANT;
    }

    /** USER 여부 */
    public boolean isUser() {
        return this.role == Role.USER;
    }

    /** MERCHANT 카테고리 가져오기 (없으면 null) */
    public Category getMerchantCategory() {
        if (!isMerchant()) return null;
        if (this.categories == null || this.categories.isEmpty()) return null;
        return this.categories.get(0);
    }

    /** 관심 카테고리 Set 형태로 가져오기 (USER 전용) */
    public Set<Category> getCategorySet() {
        return (this.categories == null) ? Set.of() : new HashSet<>(this.categories);
    }
}
