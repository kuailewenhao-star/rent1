package com.rent1.domain.member;

import java.util.List;

/**
 * 会员联系人仓储接口
 */
public interface MemberContactRepository {

    /**
     * 根据ID查找联系人
     */
    MemberContact findById(String contactId);

    /**
     * 根据会员ID查找所有联系人
     */
    List<MemberContact> findByMemberId(String memberId);

    /**
     * 根据会员ID和是否紧急联系人查找
     */
    List<MemberContact> findByMemberIdAndIsEmergency(String memberId, Boolean isEmergency);

    /**
     * 统计会员的紧急联系人数量
     */
    long countByMemberIdAndIsEmergency(String memberId, Boolean isEmergency);

    /**
     * 保存联系人
     */
    void save(MemberContact contact);

    /**
     * 更新联系人
     */
    void update(MemberContact contact);

    /**
     * 删除联系人
     */
    void deleteById(String contactId);
}
