package com.elysia.mooc.ai.stream.domain.vo;

import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SSE citation 事件数据。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamCitationVO {

    /** RAG 引用来源。 */
    private List<AiSourceVO> sources;
}
