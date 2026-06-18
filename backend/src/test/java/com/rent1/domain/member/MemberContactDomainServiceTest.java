package com.rent1.domain.member;

import com.rent1.domain.common.ContactRelationship;
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
 * 会员联系人领域服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class MemberContactDomainServiceTest {

    @Mock
    private MemberContactRepository memberContactRepository;

    @InjectMocks
    private MemberContactDomainService memberContactDomainService;

    @Test
    void addContact_shouldCreateEmergencyContact() {
        when(memberContactRepository.countByMemberIdAndIsEmergency("member_001", true)).thenReturn(0L);
        doNothing().when(memberContactRepository).save(any(MemberContact.class));

        MemberContact result = memberContactDomainService.addContact(
                "member_001",
                "encrypted_name",
                "encrypted_phone",
                "RELATIVE",
                true
        );

        assertNotNull(result);
        assertEquals("member_001", result.getMemberId());
        assertTrue(result.getIsEmergency());
        assertFalse(result.getIsDefault());
        verify(memberContactRepository).save(any(MemberContact.class));
    }

    @Test
    void addContact_shouldThrowException_whenEmergencyContactLimitReached() {
        when(memberContactRepository.countByMemberIdAndIsEmergency("member_001", true)).thenReturn(10L);

        assertThrows(RuntimeException.class,
                () -> memberContactDomainService.addContact(
                        "member_001",
                        "encrypted_name",
                        "encrypted_phone",
                        "RELATIVE",
                        true
                ));
    }

    @Test
    void getContacts_shouldReturnAllContacts() {
        MemberContact contact1 = MemberContact.builder()
                .contactId("contact_001")
                .memberId("member_001")
                .isDefault(true)
                .isEmergency(false)
                .build();
        MemberContact contact2 = MemberContact.builder()
                .contactId("contact_002")
                .memberId("member_001")
                .isDefault(false)
                .isEmergency(true)
                .build();

        when(memberContactRepository.findByMemberId("member_001"))
                .thenReturn(Arrays.asList(contact1, contact2));

        List<MemberContact> result = memberContactDomainService.getContacts("member_001");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void deleteContact_shouldDelete_whenNotDefaultAndOwnerMatch() {
        MemberContact contact = MemberContact.builder()
                .contactId("contact_001")
                .memberId("member_001")
                .isDefault(false)
                .isEmergency(true)
                .build();

        when(memberContactRepository.findById("contact_001")).thenReturn(contact);
        doNothing().when(memberContactRepository).deleteById("contact_001");

        assertDoesNotThrow(() -> memberContactDomainService.deleteContact("contact_001", "member_001"));
        verify(memberContactRepository).deleteById("contact_001");
    }

    @Test
    void deleteContact_shouldThrowException_whenDefaultContact() {
        MemberContact contact = MemberContact.builder()
                .contactId("contact_001")
                .memberId("member_001")
                .isDefault(true)
                .isEmergency(false)
                .build();

        when(memberContactRepository.findById("contact_001")).thenReturn(contact);

        assertThrows(RuntimeException.class,
                () -> memberContactDomainService.deleteContact("contact_001", "member_001"));
    }

    @Test
    void deleteContact_shouldThrowException_whenNotOwner() {
        MemberContact contact = MemberContact.builder()
                .contactId("contact_001")
                .memberId("member_001")
                .isDefault(false)
                .isEmergency(true)
                .build();

        when(memberContactRepository.findById("contact_001")).thenReturn(contact);

        assertThrows(RuntimeException.class,
                () -> memberContactDomainService.deleteContact("contact_001", "member_002"));
    }

    @Test
    void validatePhoneFormat_shouldReturnTrue_whenValidPhone() {
        assertTrue(memberContactDomainService.validatePhoneFormat("13800138000"));
        assertTrue(memberContactDomainService.validatePhoneFormat("15912345678"));
    }

    @Test
    void validatePhoneFormat_shouldReturnFalse_whenInvalidPhone() {
        assertFalse(memberContactDomainService.validatePhoneFormat("12345678901")); // 10位
        assertFalse(memberContactDomainService.validatePhoneFormat("138001380001")); // 12位
        assertFalse(memberContactDomainService.validatePhoneFormat(null));
        assertFalse(memberContactDomainService.validatePhoneFormat(""));
    }
}
