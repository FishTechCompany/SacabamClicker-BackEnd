package org.sacabam.sacabamclickerbe.databaseConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DatabaseConfigTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnectionAndDataInit() {
        // Nyanko giúp Goshujinsama kiểm tra số lượng dòng trong bảng "Roles"
        // Lưu ý: Vì tên bảng trong database là "Roles" (có ngoặc kép) nên query cũng phải để trong ngoặc kép
        Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Roles\"", Integer.class);

        // Nyanko kiểm tra số lượng dòng trong bảng "Permissions"
        Integer permissionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"Permissions\"", Integer.class);

        System.out.println("🐾 Nyanko Report 🐾");
        System.out.println("--------------------------------");
        System.out.println("Role count found: " + roleCount);
        System.out.println("Permission count found: " + permissionCount);

        // Kỳ vọng: 3 Role (User, Pro, Admin)
        assertThat(roleCount).as("Bảng Roles phải có đúng 3 dòng").isEqualTo(3);

        // Kỳ vọng: 7 Permission (Play, Switch Theme, v.v...)
        assertThat(permissionCount).as("Bảng Permissions phải có đúng 7 dòng").isEqualTo(7);
    }
}