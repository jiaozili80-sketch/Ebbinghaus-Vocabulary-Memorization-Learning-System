package com.ebbinghaus.vocab.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    private Long id;
    private Long userId;
    private String module;
    private String operation;
    private String requestUri;
    private String requestMethod;
    private String ip;
    private LocalDateTime createTime;
}
