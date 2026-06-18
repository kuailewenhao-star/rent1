package com.rent1.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent1.domain.common.MemberType;
import com.rent1.domain.member.Member;
import com.rent1.domain.member.MemberRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 会员MyBatis Mapper实现
 */
@Repository
public class MemberMybatisRepository implements MemberRepository {

    private final MemberMapper memberMapper;

    public MemberMybatisRepository(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public Member findById(String memberId) {
        return memberMapper.selectById(memberId);
    }

    @Override
    public Member findByPhoneEncryptedAndMemberType(String phoneEncrypted, MemberType memberType) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhoneEncrypted, phoneEncrypted)
                .eq(Member::getMemberType, memberType);
        return memberMapper.selectOne(wrapper);
    }

    @Override
    public Member findBySubjectIdAndMemberType(String subjectId, MemberType memberType) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getSubjectId, subjectId)
                .eq(Member::getMemberType, memberType);
        return memberMapper.selectOne(wrapper);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        // 管理员通过userId查询
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, username)
                .eq(Member::getMemberType, MemberType.ADMIN);
        return Optional.ofNullable(memberMapper.selectOne(wrapper));
    }

    @Override
    public void save(Member member) {
        // 先检查是否已存在
        Member existing = memberMapper.selectById(member.getMemberId());
        if (existing == null) {
            memberMapper.insert(member);
        } else {
            memberMapper.updateById(member);
        }
    }

    @Override
    public void update(Member member) {
        memberMapper.updateById(member);
    }
}

/**
 * 会员Mapper接口
 */
@org.apache.ibatis.annotations.Mapper
interface MemberMapper extends BaseMapper<Member> {
}
