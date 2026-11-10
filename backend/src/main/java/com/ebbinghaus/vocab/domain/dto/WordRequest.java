package com.ebbinghaus.vocab.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WordRequest {

    @NotNull(message = "所属词库不能为空")
    private Long bookId;

    @NotBlank(message = "英文单词不能为空")
    @Size(max = 100, message = "英文单词不能超过100个字符")
    private String spelling;

    @Size(max = 100, message = "音标不能超过100个字符")
    private String phonetic;

    @Size(max = 30, message = "词性不能超过30个字符")
    private String partOfSpeech;

    @NotBlank(message = "中文释义不能为空")
    @Size(max = 500, message = "中文释义不能超过500个字符")
    private String meaning;

    @Size(max = 1000, message = "英文例句不能超过1000个字符")
    private String exampleSentence;

    @Size(max = 1000, message = "例句翻译不能超过1000个字符")
    private String exampleTranslation;

    @Size(max = 255, message = "发音地址不能超过255个字符")
    private String audioUrl;

    private Integer difficulty;
    private Integer status;
}
