package com.elysia.mooc.ops.constants;

/** 运营配置模块常量。 */
public final class OpsConfigConstants {

    private OpsConfigConstants() {
    }

    /** 默认导出文件访问前缀，仅用于返回可识别的异步任务文件名。 */
    public static final String EXPORT_FILE_EXTENSION_XLSX = "xlsx";

    /** CSV 导出格式。 */
    public static final String EXPORT_FILE_EXTENSION_CSV = "csv";

    /** 默认配置分组。 */
    public static final String DEFAULT_CONFIG_GROUP = "GENERAL";

    /** 配置键最大长度，对齐数据库字段长度。 */
    public static final int CONFIG_KEY_MAX_LENGTH = 128;

    /** 配置值最大校验长度，LONGTEXT 不截断，但接口层限制异常大请求。 */
    public static final int CONFIG_VALUE_MAX_LENGTH = 20_000;
}
