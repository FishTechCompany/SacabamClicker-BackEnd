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
@Table(name = "RolePermissions")
@EntityListeners(AuditingEntityListener.class)
public class RolePermission {

    // Nhúng khóa chính phức hợp (roleId + permissionId)
    @EmbeddedId
    private RolePermissionId id;

    // --- RELATIONSHIPS ---

    // Mapping về Role
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId") // Map với thuộc tính roleId trong RolePermissionId
    @JoinColumn(name = "roleId")
    private Role role;

    // Mapping về Permission
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId") // Map với thuộc tính permissionId trong RolePermissionId
    @JoinColumn(name = "permissionId")
    private Permission permission;

    // --- OTHER COLUMNS ---

    @Column(name = "status", length = 20)
    private String status;

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

}
