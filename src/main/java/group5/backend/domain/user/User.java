package group5.backend.domain.user;

import group5.backend.exception.category.MerchantInvalidCategorySizeException;
import group5.backend.exception.category.UserInvalidCategorySizeException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Table(name="users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
//UserDetails: 스프링 시큐리티에서 사용자의 인증 정보를 담아두는 인터페이스
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",updatable = false)
    private Long id;

    @Column(name="email",nullable = false,unique = true)
    private String email;

    @Column(name="password")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_categories",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "category")
    private List<Category> categories;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //사용자가 갖고 있는 권한 목록 반환
    /**
     * GrantedAuthority: Spring Security에서 사용자의 권한(role)을 표현
     * getAuthorities(): 현재 로그인된 사용자의 권한 목록을 반환하는 메서드
     * */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    //사용자 비번 반환
    @Override
    public String getPassword() {
        return password;
    }

    //사용자 이름 반환
    @Override
    public String getUsername() {
        return email;
    }

    //계정이 만료되지 않으면 true를 반환
    @Override
    public boolean isAccountNonExpired() {
        //만료되었는지 확인하는 로직
        return true;
    }

    //계정이 잠금되었는지 확인
    @Override
    public boolean isAccountNonLocked() {
        //계정 잠금되었는지 확인하는 로직
        return true;
    }

    //비밀번호가 만료되었는지 확인
    @Override
    public boolean isCredentialsNonExpired() {
        //패스워드가 만료되었는지 확인하는 로직
        return true;
    }

    //계정이 사용 가능한지 확인
    @Override
    public boolean isEnabled() {
        //계정이 사용 가능한지 확인하는 로직
        return true;
    }

    //Role이 user인 경우 카테고리 설정
    public void toggleCategory(Category category) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }

        if (this.categories.contains(category)) {
            // 최소 1개 유지 조건 추가
            if (this.categories.size() == 1) {
                throw new UserInvalidCategorySizeException(
                        "카테고리는 최소 1개 이상 선택해야 합니다.",
                        new ArrayList<>(this.categories)
                );
            }
            this.categories.remove(category); // 토글 OFF
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


    //Role이 merchant인 경우 카테고리 설정하는 메서드
    public void setMerchantCategory(Category category) {
        if (category == null) {
            throw new MerchantInvalidCategorySizeException("가게 카테고리는 반드시 1개의 카테고리를 설정해야 합니다.");
        }

        this.categories = List.of(category); // 무조건 1개 설정
    }


}