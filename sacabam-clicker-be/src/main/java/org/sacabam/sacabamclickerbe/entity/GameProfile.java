package org.sacabam.sacabamclickerbe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GameProfiles")
@EntityListeners(AuditingEntityListener.class)
public class GameProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // --- RELATIONSHIPS ---

    // Quan hệ 1-1 với User
    // optional = false: Bắt buộc phải có User mới tạo được Profile
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false, unique = true, referencedColumnName = "id")
    private User user;

    // --- GAME DATA ---

    @Column(name = "displayName", length = 100)
    private String displayName;

    @Column(name = "avatarUrl", length = 255)
    private String avatarUrl;

    // Khởi tạo giá trị mặc định để tránh null và khớp với DB
    @Column(name = "currentScore")
    private Long currentScore = 0L;

    @Column(name = "clickPower")
    private Integer clickPower = 1;

    @Column(name = "upgradeLevel")
    private Integer upgradeLevel = 1;

    // --- STATUS & AUDIT ---

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "lastActiveAt")
    private LocalDateTime lastActiveAt;

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
