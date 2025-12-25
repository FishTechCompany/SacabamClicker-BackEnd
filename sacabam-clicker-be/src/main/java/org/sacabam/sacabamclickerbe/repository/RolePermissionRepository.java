package org.sacabam.sacabamclickerbe.repository;

import org.sacabam.sacabamclickerbe.entity.RolePermission;
import org.sacabam.sacabamclickerbe.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("SELECT rp FROM RolePermission rp " +
            "LEFT JOIN FETCH rp.permission " +
            "WHERE rp.role.id = :roleId AND rp.status = 'ACTIVE'")
    List<RolePermission> findActivePermissionsByRoleId(@Param("roleId") Integer roleId);
}