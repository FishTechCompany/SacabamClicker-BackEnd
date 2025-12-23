package org.sacabam.sacabamclickerbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ScoreLogs")
@EntityListeners(AuditingEntityListener.class)
public class ScoreLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // Dùng Long vì DB là bigint

    // --- RELATIONSHIPS ---

    // Nhiều Log thuộc về 1 GameProfile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameProfileId", nullable = false, referencedColumnName = "id")
    private GameProfile gameProfile;

    // --- LOG DATA ---

    @Column(name = "actionType", nullable = false, length = 50)
    private String actionType; // Ví dụ: "CLICK", "UPGRADE", "BONUS"

    @Column(name = "scoreChange", nullable = false)
    private Long scoreChange; // Điểm cộng thêm hoặc trừ đi

    @Column(name = "scoreAfter", nullable = false)
    private Long scoreAfter; // Điểm số sau khi thay đổi (Snapshot)

    // --- AUDIT ---

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

}