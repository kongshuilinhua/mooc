package com.elysia.mooc.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.media.domain.po.MediaDocumentPO;
import org.apache.ibatis.annotations.Mapper;

/** 文档媒资数据库访问接口。 */
@Mapper
public interface MediaDocumentMapper extends BaseMapper<MediaDocumentPO> {
}
