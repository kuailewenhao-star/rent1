package com.rent1.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent1.domain.member.MemberContact;
import com.rent1.domain.member.MemberContactRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会员联系人MyBatis Mapper实现
 */
@Repository
public class MemberContactMybatisRepository implements MemberContactRepository {

    private final MemberContactMapper memberContactMapper;

    public MemberContactMybatisRepository(MemberContactMapper memberContactMapper) {
        this.memberContactMapper = memberContactMapper;
    }

    @Override
    public MemberContact findById(String contactId) {
        return memberContactMapper.selectById(contactId);
    }

    @Override
    public List<MemberContact> findByMemberId(String memberId) {
        LambdaQueryWrapper<MemberContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberContact::getMemberId, memberId)
                .orderByAsc(MemberContact::getCreateTime);
        return memberContactMapper.selectList(wrapper);
    }

    @Override
    public List<MemberContact> findByMemberIdAndIsEmergency(String memberId, Boolean isEmergency) {
        LambdaQueryWrapper<MemberContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberContact::getMemberId, memberId)
                .eq(MemberContact::getIsEmergency, isEmergency);
        return memberContactMapper.selectList(wrapper);
    }

    @Override
    public long countByMemberIdAndIsEmergency(String memberId, Boolean isEmergency) {
        LambdaQueryWrapper<MemberContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberContact::getMemberId, memberId)
                .eq(MemberContact::getIsEmergency, isEmergency);
        return memberContactMapper.selectCount(wrapper);
    }

    @Override
    public void save(MemberContact contact) {
        if (contact.getContactId() == null) {
            memberContactMapper.insert(contact);
        } else {
            memberContactMapper.updateById(contact);
        }
    }

    @Override
    public void update(MemberContact contact) {
        memberContactMapper.updateById(contact);
    }

    @Override
    public void deleteById(String contactId) {
        memberContactMapper.deleteById(contactId);
    }
}

/**
 * 会员联系人Mapper接口
 */
@org.apache.ibatis.annotations.Mapper
interface MemberContactMapper extends BaseMapper<MemberContact> {
}
