package com.rent1.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rent1.domain.property.entity.Room;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间Mapper接口
 *
 * 使用MyBatis-Plus BaseMapper提供基础CRUD操作
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

}
