package org.sacabam.sacabamclickerbe.repository;

import org.sacabam.sacabamclickerbe.entity.GameProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameProfileRepository extends JpaRepository<GameProfile, Integer> {

    Optional<GameProfile> findByUserId(Integer userId);

    @Query("SELECT gp FROM GameProfile gp LEFT JOIN FETCH gp.user WHERE gp.user.id = :userId")
    Optional<GameProfile> findByUserIdWithUser(@Param("userId") Integer userId);
}