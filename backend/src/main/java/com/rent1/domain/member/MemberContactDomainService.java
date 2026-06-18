package com.rent1.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员联系人领域服务
 */
@Service
@RequiredArgsConstructor
public class MemberContactDomainService {

    private final MemberContactRepository memberContactRepository;

    /**
     * 添加联系人
     */
    public MemberContact addContact(String memberId, String nameEncrypted, String phoneEncrypted,
                                    String relationshipCode, Boolean isEmergency) {
        // 紧急联系人至少需要1条（合约创建强校验）
        if (Boolean.TRUE.equals(isEmergency)) {
            long emergencyCount = memberContactRepository.countByMemberIdAndIsEmergency(memberId, true);
            if (emergencyCount >= 10) {
                throw new RuntimeException("紧急联系人最多10条");
            }
        }

        MemberContact contact = MemberContact.builder()
                .contactId(java.util.UUID.randomUUID().toString())
                .memberId(memberId)
                .nameEncrypted(nameEncrypted)
                .phoneEncrypted(phoneEncrypted)
                .isDefault(false)
                .isEmergency(isEmergency)
                .build();

        memberContactRepository.save(contact);
        return contact;
    }

    /**
     * 获取会员的所有联系人
     */
    public List<MemberContact> getContacts(String memberId) {
        return memberContactRepository.findByMemberId(memberId);
    }

    /**
     * 获取会员的紧急联系人
     */
    public List<MemberContact> getEmergencyContacts(String memberId) {
        return memberContactRepository.findByMemberIdAndIsEmergency(memberId, true);
    }

    /**
     * 删除联系人
     */
    public void deleteContact(String contactId, String memberId) {
        MemberContact contact = memberContactRepository.findById(contactId);
        if (contact == null) {
            throw new RuntimeException("联系人不存在");
        }
        if (!contact.getMemberId().equals(memberId)) {
            throw new RuntimeException("无权删除此联系人");
        }
        if (Boolean.TRUE.equals(contact.getIsDefault())) {
            throw new RuntimeException("默认联系人不可删除");
        }
        memberContactRepository.deleteById(contactId);
    }

    /**
     * 校验联系人手机号格式
     */
    public boolean validatePhoneFormat(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }
}
