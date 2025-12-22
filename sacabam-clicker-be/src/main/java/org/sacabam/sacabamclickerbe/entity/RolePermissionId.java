package org.sacabam.sacabamclickerbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable // Đánh dấu đây là class dùng để nhúng làm ID
public class RolePermissionId implements Serializable {

    @Column(name = "roleId")
    private Integer roleId;

    @Column(name = "permissionId")
    private Integer permissionId;
}