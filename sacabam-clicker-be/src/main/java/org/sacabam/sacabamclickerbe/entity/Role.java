package org.sacabam.sacabamclickerbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Roles")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    // Vì bảng User dùng int, Role dùng int, nên ở đây mapping là Integer
    // Nếu ID trong DB không tự tăng (do Goshujinsama tự insert ID), có thể bỏ @GeneratedValue
    // Nhưng thường Roles vẫn nên để Identity hoặc Auto.
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "status", length = 20)
    private String status;

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    // --- RELATIONSHIPS ---

    // mappedBy = "role": Trỏ tới field "role" trong class User
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude // Ngắt chuỗi toString của Lombok để tránh StackOverflow
    @JsonIgnore       // Ngắt JSON để tránh vòng lặp vô tận
    private List<User> users;

    //@ManyToMany through RolePermisson
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<RolePermission> rolePermissions;
}