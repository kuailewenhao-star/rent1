package com.rent1.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent1.infrastructure.persistence.po.NotificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<NotificationPO> {

    /**
     * 批量插入消息
     */
    void insertBatch(@Param("list") List<NotificationPO> list);
}