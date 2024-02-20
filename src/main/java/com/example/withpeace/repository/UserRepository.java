package com.example.withpeace.repository;


import com.example.withpeace.domain.User;
import com.example.withpeace.type.EProvider;
import com.example.withpeace.type.ERole;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<UserSecurityForm> findByIdAndIsLoginAndRefreshTokenIsNotNull(Long id, boolean b);

    Optional<UserSecurityForm> findByRefreshToken(String refreshToken);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :refreshToken, u.isLogin = :status WHERE u.id = :id")
    void updateRefreshTokenAndLoginStatus(Long id, String refreshToken, boolean status);

    @Query("SELECT u FROM User u WHERE u.socialId = :socialId AND u.eProvider = :loginProvider")
    Optional<User> findBySocialIdAndEProvider(String socialId, EProvider loginProvider);


    interface UserSecurityForm {

        static UserSecurityForm invoke(User user) {


            return new UserSecurityForm() {
                @Override
                public Long getId() {
                    return user.getId();
                }

                @Override
                public ERole getRole() {
                    return user.getRole();
                }
            };
        }

        Long getId();

        ERole getRole();
    }
}
