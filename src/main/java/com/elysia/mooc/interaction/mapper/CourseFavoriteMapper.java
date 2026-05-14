package com.elysia.mooc.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.interaction.domain.po.CourseFavoritePO;
import org.apache.ibatis.annotations.Mapper;

/** 课程收藏表数据库访问接口。 */
@Mapper
public interface CourseFavoriteMapper extends BaseMapper<CourseFavoritePO> {
}
