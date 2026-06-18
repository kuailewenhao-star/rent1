package com.rent1.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 脱敏服务单元测试
 * <p>
 * 覆盖：手机号、身份证号、姓名脱敏；管理员模式（明文）；空/null 处理
 * </p>
 */
class DesensitizationServiceTest {

    private final DesensitizationService service = new DesensitizationService();

    @Test
    void maskPhone_11位手机号脱敏() {
        assertEquals("138****8000", service.maskPhone("13800138000"));
    }

    @Test
    void maskPhone_短号正常处理() {
        // 不具备 11 位时也返回脱敏
        String result = service.maskPhone("1380013");
        assertNotNull(result);
    }

    @Test
    void maskPhone_null原样返回() {
        assertNull(service.maskPhone(null));
    }

    @Test
    void maskIdCard_18位身份证脱敏() {
        assertEquals("440101********1234", service.maskIdCard("440101199001011234"));
    }

    @Test
    void maskIdCard_null原样返回() {
        assertNull(service.maskIdCard(null));
    }

    @Test
    void maskName_双字姓名_保留首字() {
        assertEquals("张*", service.maskName("张三"));
    }

    @Test
    void maskName_三字姓名_首字其余星号() {
        assertEquals("张**", service.maskName("张三丰"));
    }

    @Test
    void maskName_单字姓名_追加1位星号() {
        assertEquals("张*", service.maskName("张"));
    }

    @Test
    void maskName_null为空值不抛异常() {
        assertNull(service.maskName(null));
        assertEquals("", service.maskName(""));
    }

    @Test
    void adminMode_返回明文() {
        try {
            service.setAdminMode(true);
            assertEquals("13800138000", service.maskPhone("13800138000"));
            assertEquals("440101199001011234", service.maskIdCard("440101199001011234"));
            assertEquals("张三", service.maskName("张三"));
        } finally {
            service.clearAdminMode();
        }
    }
}
