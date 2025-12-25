package org.sacabam.sacabamclickerbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users") // Ánh xạ vào bảng "Users"
@EntityListeners(AuditingEntityListener.class) // Kích hoạt tính năng Audit tự động
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tương ứng với int PK tự tăng (nếu dùng serial)
    @Column(name = "id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    @JsonIgnore // Bảo mật: Không trả về password trong JSON
    private String password;

    // Mapping quan hệ: User n - 1 Role
    // FetchType.LAZY: Chỉ lấy thông tin Role khi được gọi (getter), giúp tối ưu hiệu năng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", nullable = false, referencedColumnName = "id")
    private Role role;

    @Column(name = "status", length = 20)
    private String status;

    @CreatedDate
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
