# Mermaid UML 图使用说明

本文件夹包含 8 个 Mermaid 格式的 UML 图源文件（.mmd）。

## 文件列表

| 文件名 | 说明 | 插入位置 |
|--------|------|----------|
| 01_需求分析_用例图.mmd | 用例图 | 需求分析.doc → 八、UML 用例图 |
| 02_需求分析_活动图.mmd | 活动图 | 需求分析.doc → 九、UML 活动图 |
| 03_总体设计_组件图.mmd | 组件图 | 总体设计.doc → 七、UML 组件图 |
| 04_总体设计_包图.mmd | 包图 | 总体设计.doc → 八、UML 包图 |
| 05_总体设计_部署图.mmd | 部署图 | 总体设计.doc → 九、UML 部署图 |
| 06_详细设计_类图.mmd | 类图 | 详细设计.doc → 三、UML 类图 |
| 07_详细设计_时序图.mmd | 时序图 | 详细设计.doc → 八、UML 时序图 |
| 08_详细设计_ER图.mmd | ER图 | 详细设计.doc → 五、UML ER 图 |

## 使用方法

### 方法 1：在线编辑器（推荐）

1. 访问 https://mermaid.live/
2. 复制 .mmd 文件内容粘贴到编辑器
3. 点击右上角 "Actions" → "PNG" 或 "SVG" 导出图片
4. 在 Word 中插入导出的图片

### 方法 2：VS Code

1. 安装插件 "Markdown Preview Mermaid Support"
2. 创建 .md 文件，用 ```mermaid 代码块包裹 .mmd 文件内容
3. 预览后截图或导出

### 方法 3：Typora

1. 打开 Typora
2. 创建 .md 文件，用 ```mermaid 代码块包裹 .mmd 文件内容
3. 会自动渲染，可以直接复制图片

### 方法 4：命令行工具

```bash
# 安装 mermaid-cli
npm install -g @mermaid-js/mermaid-cli

# 转换为 PNG
mmdc -i 01_需求分析_用例图.mmd -o 01_需求分析_用例图.png

# 批量转换
for file in *.mmd; do mmdc -i "$file" -o "${file%.mmd}.png"; done
```

## 在线预览

也可以直接打开 `查看所有UML图.html` 在浏览器中预览所有图片（SVG 格式）。

## 注意事项

- Mermaid 图在不同渲染器中可能略有差异
- 建议使用 mermaid.live 导出，效果最稳定
- 导出的 PNG 图片可以直接插入 Word 文档
