CREATE TABLE IF NOT EXISTS ops_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人名称',
  action VARCHAR(64) NOT NULL COMMENT '操作类型',
  target_type VARCHAR(64) NOT NULL COMMENT '目标类型',
  target_id VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
  request_method VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  request_path VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
  request_ip VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  trace_id VARCHAR(64) DEFAULT NULL COMMENT 'traceId',
  success TINYINT NOT NULL DEFAULT 1 COMMENT '0失败 1成功',
  error_message VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
  cost_ms INT DEFAULT NULL COMMENT '耗时毫秒',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_audit_operator_time (operator_id, create_time),
  KEY idx_audit_target (target_type, target_id),
  KEY idx_audit_trace (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计日志表';

CREATE TABLE IF NOT EXISTS idempotent_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  idempotent_key VARCHAR(128) NOT NULL COMMENT '幂等键',
  biz_type VARCHAR(64) NOT NULL COMMENT '业务类型',
  biz_id VARCHAR(64) DEFAULT NULL COMMENT '业务ID',
  request_hash VARCHAR(128) DEFAULT NULL COMMENT '请求摘要',
  response_body TEXT DEFAULT NULL COMMENT '首次响应',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0处理中 1成功 2失败',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_idempotent_key (idempotent_key),
  KEY idx_idempotent_expire (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='幂等记录表';
