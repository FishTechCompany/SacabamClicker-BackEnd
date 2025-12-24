package org.sacabam.sacabamclickerbe.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Util Tests")
class EmailUtilTest {

    @InjectMocks
    private EmailUtil emailUtil;

    @Test
    @DisplayName("Email hợp lệ - Các trường hợp đúng")
    void isValidEmail_Success_WithValidEmails() {
        // Valid emails
        assertTrue(emailUtil.isValidEmail("test@gmail.com"));
        assertTrue(emailUtil.isValidEmail("user.name@yahoo.com"));
        assertTrue(emailUtil.isValidEmail("test123@hotmail.com"));
        assertTrue(emailUtil.isValidEmail("user+tag@outlook.com"));
        assertTrue(emailUtil.isValidEmail("test_user@company.co.uk"));
    }

    @Test
    @DisplayName("Email không hợp lệ - Định dạng sai")
    void isValidEmail_Failure_WithInvalidFormat() {
        // Invalid formats
        assertFalse(emailUtil.isValidEmail("invalid-email"));
        assertFalse(emailUtil.isValidEmail("@gmail.com"));
        assertFalse(emailUtil.isValidEmail("test@"));
        assertFalse(emailUtil.isValidEmail("test..test@gmail.com"));
        assertFalse(emailUtil.isValidEmail(".test@gmail.com"));
        assertFalse(emailUtil.isValidEmail("test.@gmail.com"));
        assertFalse(emailUtil.isValidEmail("test@gmail"));
        assertFalse(emailUtil.isValidEmail("test@.com"));
        assertFalse(emailUtil.isValidEmail("test@gmail."));
    }

    @Test
    @DisplayName("Email không hợp lệ - Trường hợp domain lạ")
    void isValidEmail_Failure_WithWeirdDomains() {
        // Weird domains that should be rejected
        assertFalse(emailUtil.isValidEmail("test@gmail.com.com"));
        assertFalse(emailUtil.isValidEmail("test@yahoo.net.net"));
        assertFalse(emailUtil.isValidEmail("test@hotmail.org.org"));
        assertFalse(emailUtil.isValidEmail("kien2862004@gmail.com.com"));
        assertFalse(emailUtil.isValidEmail("user@domain..com"));
        assertFalse(emailUtil.isValidEmail("user@-domain.com"));
        assertFalse(emailUtil.isValidEmail("user@domain-.com"));
    }

    @Test
    @DisplayName("Email không hợp lệ - Null và empty")
    void isValidEmail_Failure_WithNullAndEmpty() {
        assertFalse(emailUtil.isValidEmail(null));
        assertFalse(emailUtil.isValidEmail(""));
        assertFalse(emailUtil.isValidEmail("   "));
    }

    @Test
    @DisplayName("Email không hợp lệ - Quá dài")
    void isValidEmail_Failure_WithTooLong() {
        // Email quá dài (> 254 ký tự)
        String longEmail = "a".repeat(250) + "@gmail.com";
        assertFalse(emailUtil.isValidEmail(longEmail));

        // Local part quá dài (> 64 ký tự)
        String longLocalPart = "a".repeat(65) + "@gmail.com";
        assertFalse(emailUtil.isValidEmail(longLocalPart));
    }

    @Test
    @DisplayName("Kiểm tra domain phổ biến")
    void isCommonDomain_Success() {
        assertTrue(emailUtil.isCommonDomain("test@gmail.com"));
        assertTrue(emailUtil.isCommonDomain("user@yahoo.com"));
        assertTrue(emailUtil.isCommonDomain("test@hotmail.com"));
        assertTrue(emailUtil.isCommonDomain("user@outlook.com"));

        assertFalse(emailUtil.isCommonDomain("test@company.com"));
        assertFalse(emailUtil.isCommonDomain("user@university.edu"));
        assertFalse(emailUtil.isCommonDomain("invalid-email"));
    }

    @Test
    @DisplayName("Chuẩn hóa email")
    void normalizeEmail_Success() {
        assertEquals("test@gmail.com", emailUtil.normalizeEmail("  TEST@Gmail.Com  "));
        assertEquals("user@yahoo.com", emailUtil.normalizeEmail("User@YAHOO.COM"));
        assertNull(emailUtil.normalizeEmail(null));
    }

    @Test
    @DisplayName("Lấy domain từ email")
    void getDomain_Success() {
        assertEquals("gmail.com", emailUtil.getDomain("test@gmail.com"));
        assertEquals("yahoo.com", emailUtil.getDomain("user@yahoo.com"));
        assertNull(emailUtil.getDomain("invalid-email"));
        assertNull(emailUtil.getDomain(null));
    }

    @Test
    @DisplayName("Các trường hợp edge case")
    void isValidEmail_EdgeCases() {
        // TLD ngắn
        assertFalse(emailUtil.isValidEmail("test@domain.a"));

        // TLD chỉ có số
        assertFalse(emailUtil.isValidEmail("test@domain.123"));

        // TLD có ký tự đặc biệt
        assertFalse(emailUtil.isValidEmail("test@domain.c-m"));

        // Domain có ký tự đặc biệt không hợp lệ
        assertFalse(emailUtil.isValidEmail("test@do_main.com"));

        // Email có khoảng trắng
        assertFalse(emailUtil.isValidEmail("test @gmail.com"));
        assertFalse(emailUtil.isValidEmail("test@ gmail.com"));
    }
}