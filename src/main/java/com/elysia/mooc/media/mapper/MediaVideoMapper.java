package com.elysia.mooc.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.media.domain.po.MediaVideoPO;
import org.apache.ibatis.annotations.Mapper;

/** 视频媒资数据库访问接口。 */
@Mapper
public interface MediaVideoMapper extends BaseMapper<MediaVideoPO> {
}
