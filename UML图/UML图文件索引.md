# UML 图文件索引与文档插入位置

本目录下已经生成 8 个 SVG 格式 UML 图文件，可以直接用浏览器打开，也可以插入到 Word/WPS 文档中。

## 一、需求分析.doc

| 插入章节 | 图片文件 | 说明 |
|---|---|---|
| 八、UML 用例图 | 01_需求分析_用例图.svg | 展示普通用户、管理员、系统参与者及对应功能用例 |
| 九、UML 活动图：用户学习流程 | 02_需求分析_活动图.svg | 展示用户从登录、创建计划、学习、复习到打卡的流程 |

## 二、总体设计.doc

| 插入章节 | 图片文件 | 说明 |
|---|---|---|
| 七、UML 组件图 | 03_总体设计_组件图.svg | 展示前端、后端、MySQL、Redis 的组件关系 |
| 八、UML 包图 | 04_总体设计_包图.svg | 展示后端 common、config、controller、service、mapper 等包结构 |
| 九、UML 部署图 | 05_总体设计_部署图.svg | 展示浏览器、前端服务、后端服务、MySQL、Redis 的部署关系 |

注意：刚才 `总体设计.doc` 被系统提示正在被占用，可能是你用 Word/WPS 打开了它。请关闭该文档后，我可以继续把插图位置说明写入文档顶部。

## 三、详细设计.doc

| 插入章节 | 图片文件 | 说明 |
|---|---|---|
| 三、UML 类图 | 06_详细设计_类图.svg | 展示 User、StudyPlan、WordBook、Word、StudyRecord、ReviewSchedule 等核心类关系 |
| 八、UML 时序图：提交学习结果 | 07_详细设计_时序图.svg | 展示提交学习结果时用户、前端、Controller、Service、数据库之间的调用顺序 |
| 五、UML ER 图 | 08_详细设计_ER图.svg | 展示 sys_user、word_book、word、study_record、review_schedule 等表关系 |

## 四、使用方法

1. 打开对应 Word 文档。
2. 找到 UML 章节。
3. 选择“插入图片”。
4. 从 `UML图` 文件夹选择对应 SVG 文件。
5. 插入后可删除文档中原来的文本版 UML 图。

如果 Word/WPS 不支持 SVG，可以用浏览器打开 SVG 后截图，或者另存为 PNG 后插入。
