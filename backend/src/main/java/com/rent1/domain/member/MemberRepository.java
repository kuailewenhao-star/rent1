package com.rent1.domain.member;

import com.rent1.domain.common.MemberType;
import java.util.Optional;

/**
 * 会员仓储接口
 */
public interface MemberRepository {

    /**
     * 根据ID查找会员
     */
    Member findById(String memberId);

    /**
     * 根据手机号加密值和角色类型查找会员
     */
    Member findByPhoneEncryptedAndMemberType(String phoneEncrypted, MemberType memberType);

    /**
     * 根据主体ID和角色类型查找会员
     */
    Member findBySubjectIdAndMemberType(String subjectId, MemberType memberType);

    /**
     * 根据用户名查找（管理员登录）
     */
    Optional<Member> findByUsername(String username);

    /**
     * 保存会员
     */
    void save(Member member);

    /**
     * 更新会员
     */
    void update(Member member);
}
