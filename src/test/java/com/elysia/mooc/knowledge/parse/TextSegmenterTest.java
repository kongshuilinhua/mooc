package com.elysia.mooc.knowledge.parse;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** 文本切片器测试。 */
class TextSegmenterTest {

    @Test
    void segmentShouldUseMaxLengthAndOverlap() {
        SegmentConfig config = new SegmentConfig();
        config.setMaxLength(20);
        config.setOverlapLength(5);
        TextSegmenter segmenter = new TextSegmenter(config);

        var segments = segmenter.segment("第一段内容用于测试切片长度。\n\n第二段内容用于验证重叠窗口。");

        assertThat(segments).hasSizeGreaterThan(1);
        assertThat(segments.get(0).segmentIndex()).isEqualTo(1);
        assertThat(segments.get(0).content().length()).isLessThanOrEqualTo(20);
        assertThat(segments.get(0).metadata()).contains("\"overlapLength\":5");
    }

    @Test
    void segmentShouldReturnEmptyWhenTextBlank() {
        SegmentConfig config = new SegmentConfig();
        TextSegmenter segmenter = new TextSegmenter(config);

        assertThat(segmenter.segment(" \n \n ")).isEmpty();
    }
}
