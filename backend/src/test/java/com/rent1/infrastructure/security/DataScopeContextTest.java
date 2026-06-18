package com.rent1.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataScopeContext 线程本地上下文测试
 */
class DataScopeContextTest {

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    void setAndGet_shouldReturnScopedValue() {
        DataScopeContext.set("m1", "LANDLORD");
        assertEquals("m1", DataScopeContext.getMemberId());
        assertEquals("LANDLORD", DataScopeContext.getMemberType());
        assertTrue(DataScopeContext.isLandlord());
        assertFalse(DataScopeContext.isTenant());
        assertFalse(DataScopeContext.isAdmin());
    }

    @Test
    void clear_shouldReset() {
        DataScopeContext.set("m2", "TENANT");
        DataScopeContext.clear();
        assertNull(DataScopeContext.getMemberId());
        assertNull(DataScopeContext.getMemberType());
    }

    @Test
    void initialState_noUser_markedNone() {
        assertNull(DataScopeContext.getMemberId());
        assertFalse(DataScopeContext.isLandlord());
        assertFalse(DataScopeContext.isTenant());
        assertFalse(DataScopeContext.isAdmin());
    }
}
