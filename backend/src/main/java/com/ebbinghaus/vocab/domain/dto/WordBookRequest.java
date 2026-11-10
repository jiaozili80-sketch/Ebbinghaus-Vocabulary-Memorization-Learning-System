package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WordBookRequest {

    @NotBlank(message = "词库名称不能为空")
    @Size(max = 100, message = "词库名称不能超过100个字符")
    private String name;

    @NotBlank(message = "词库分类不能为空")
    @Size(max = 50, message = "词库分类不能超过50个字符")
    private String category;

    @Size(max = 500, message = "词库描述不能超过500个字符")
    private String description;

    @NotNull(message = "词库类型不能为空")
    private Integer type;

    @NotNull(message = "词库状态不能为空")
    private Integer status;
}
