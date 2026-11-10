# 艾宾浩斯单词背诵学习系统

基于 Spring Boot + MyBatis-Plus + MySQL + Redis + Vue3 + Element Plus + ECharts 的前后端分离单词学习系统。

## 项目结构

```text
.
├── backend   # Spring Boot 后端
└── frontend  # Vue3 前端
```

## 后端技术栈

- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7
- MySQL
- Redis
- Knife4j
- JWT

## 前端技术栈

- Vue 3
- Vite
- TypeScript
- Element Plus
- ECharts
- Pinia
- Axios

## 已搭建功能

- 后端基础工程
- MySQL / Redis 基础配置
- 统一响应对象
- 用户、词库、单词、复习计划、学习记录基础实体
- MyBatis-Plus Mapper
- 艾宾浩斯复习周期计算服务
- 健康检查接口
- 词库列表接口
- 数据库初始化 SQL
- 前端基础工程
- 前台布局、学习总览、词库中心、背诵页、统计页
- Axios 请求封装
- ECharts 学习趋势图

## 数据库初始化

1. 创建 MySQL 数据库并执行：

```sql
source backend/src/main/resources/db/schema.sql;
```

2. 通过环境变量提供运行配置，不要把密码或 JWT 密钥写入仓库：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="至少32字节的随机密钥"
```

Redis 用于保存当前登录会话并实现退出失效，后端启动前必须保证 Redis 可用。

首次部署如需自动创建管理员，需要显式启用并提供至少 12 位的初始密码：

```powershell
$env:ADMIN_BOOTSTRAP_ENABLED="true"
$env:ADMIN_BOOTSTRAP_USERNAME="admin"
$env:ADMIN_BOOTSTRAP_PASSWORD="请替换为强密码"
```

管理员创建成功后，后续启动可以将 `ADMIN_BOOTSTRAP_ENABLED` 设为 `false`。
如果旧数据库仍存在 `admin/admin123`，后端会拒绝启动；按上述方式启用一次管理员初始化即可把旧密码替换为新的强密码。

## 启动后端

进入 `backend` 目录：

```bash
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080/api
```

健康检查：

```text
http://localhost:8080/api/health
```

Knife4j 文档地址通常为：

```text
http://localhost:8080/api/doc.html
```

## 启动前端

进入 `frontend` 目录：

```bash
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

## 功能概览

- 登录注册、JWT 与 Redis 会话鉴权
- 管理员、词库、单词和公告管理
- 艾宾浩斯复习任务与学习计划
- 生词强化、学习打卡、日历和统计图表
