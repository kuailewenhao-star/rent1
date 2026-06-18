package com.rent1.domain.member;

import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.domain.common.MemberStatus;
import com.rent1.domain.common.MemberType;
import com.rent1.infrastructure.security.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 会员领域服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class MemberDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberContactRepository memberContactRepository;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private MemberDomainService memberDomainService;

    private Member testMember;
    private String phoneEncrypted = "encrypted_phone_13800138000";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .memberId("member_001")
                .projectId("default")
                .subjectId("subject_001")
                .userId("openid_001")
                .memberType(MemberType.TENANT)
                .phoneEncrypted(phoneEncrypted)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    @Test
    void findByPhoneAndType_shouldReturnMember_whenExists() {
        when(memberRepository.findByPhoneEncryptedAndMemberType(phoneEncrypted, MemberType.TENANT))
                .thenReturn(testMember);

        Member result = memberDomainService.findByPhoneAndType(phoneEncrypted, MemberType.TENANT);

        assertNotNull(result);
        assertEquals("member_001", result.getMemberId());
        assertEquals(MemberType.TENANT, result.getMemberType());
    }

    @Test
    void findByPhoneAndType_shouldReturnNull_whenNotExists() {
        when(memberRepository.findByPhoneEncryptedAndMemberType(anyString(), any(MemberType.class)))
                .thenReturn(null);

        Member result = memberDomainService.findByPhoneAndType("nonexistent", MemberType.TENANT);

        assertNull(result);
    }

    @Test
    void findById_shouldReturnMember_whenExists() {
        when(memberRepository.findById("member_001")).thenReturn(testMember);

        Member result = memberDomainService.findById("member_001");

        assertNotNull(result);
        assertEquals("member_001", result.getMemberId());
    }

    @Test
    void findById_shouldThrowException_whenNotExists() {
        when(memberRepository.findById("nonexistent")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberDomainService.findById("nonexistent"));

        assertEquals(ErrorCode.A002.getCode(), exception.getCode());
    }

    @Test
    void createMemberIfNotExists_shouldCreateNewMember_whenNotExists() {
        when(memberRepository.findByPhoneEncryptedAndMemberType(phoneEncrypted, MemberType.TENANT))
                .thenReturn(null);
        doNothing().when(memberRepository).save(any(Member.class));

        Member result = memberDomainService.createMemberIfNotExists(phoneEncrypted, "openid_001", MemberType.TENANT);

        assertNotNull(result);
        assertEquals(MemberType.TENANT, result.getMemberType());
        assertEquals(MemberStatus.ACTIVE, result.getStatus());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void createMemberIfNotExists_shouldReturnExistingMember_whenExists() {
        when(memberRepository.findByPhoneEncryptedAndMemberType(phoneEncrypted, MemberType.TENANT))
                .thenReturn(testMember);

        Member result = memberDomainService.createMemberIfNotExists(phoneEncrypted, "openid_001", MemberType.TENANT);

        assertNotNull(result);
        assertEquals("member_001", result.getMemberId());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void validateLoginStatus_shouldThrowException_whenAccountDisabled() {
        testMember.setStatus(MemberStatus.DISABLED);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberDomainService.validateLoginStatus(testMember));

        assertEquals(ErrorCode.A003.getCode(), exception.getCode());
    }

    @Test
    void validateLoginStatus_shouldPass_whenAccountActive() {
        testMember.setStatus(MemberStatus.ACTIVE);

        assertDoesNotThrow(() -> memberDomainService.validateLoginStatus(testMember));
    }

    @Test
    void hasEmergencyContact_shouldReturnTrue_whenExists() {
        when(memberContactRepository.countByMemberIdAndIsEmergency("member_001", true)).thenReturn(1L);

        boolean result = memberDomainService.hasEmergencyContact("member_001");

        assertTrue(result);
    }

    @Test
    void hasEmergencyContact_shouldReturnFalse_whenNotExists() {
        when(memberContactRepository.countByMemberIdAndIsEmergency("member_001", true)).thenReturn(0L);

        boolean result = memberDomainService.hasEmergencyContact("member_001");

        assertFalse(result);
    }

    @Test
    void getContacts_shouldReturnContactList() {
        MemberContact contact = MemberContact.builder()
                .contactId("contact_001")
                .memberId("member_001")
                .nameEncrypted("encrypted_name")
                .phoneEncrypted("encrypted_phone")
                .isDefault(true)
                .isEmergency(false)
                .build();
        
        when(memberContactRepository.findByMemberId("member_001")).thenReturn(Arrays.asList(contact));

        List<MemberContact> result = memberDomainService.getContacts("member_001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("contact_001", result.get(0).getContactId());
    }
}
