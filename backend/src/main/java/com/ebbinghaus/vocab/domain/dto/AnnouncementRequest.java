package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementRequest {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题不能超过100个字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 5000, message = "公告内容不能超过5000个字符")
    private String content;

    @NotBlank(message = "公告类型不能为空")
    @Size(max = 30, message = "公告类型不能超过30个字符")
    private String type;

    @NotNull(message = "公告状态不能为空")
    private Integer status;
}
