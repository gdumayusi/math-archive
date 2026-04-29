# 考研数学真题分类管理系统

基于 [原型.html](/C:/Users/杜路明/Desktop/project/原型.html) 拆分实现：

- 前端：Vue 3 + Vite + Vue Router
- 后端：Spring Boot 3 + Spring Data JPA
- 数据库：MySQL

## 目录结构

- `frontend`：Vue 3 前端工程
- `backend`：Spring Boot 后端工程
- `backend/db/init.sql`：MySQL 初始化脚本

## 已实现页面

- 登录页
- 用户注册
- 总览看板
- 真题检索
- 学习统计
- 收藏题库
- 错题档案
- 管理端题目录入管理
- 管理端分类体系规划
- 管理端用户管理

## 默认账号

以下账号已写入 [init.sql](/C:/Users/杜路明/Desktop/project/backend/db/init.sql)：

- 管理员：`admin / admin`
- 考生：`student_01 / password`

说明：

- 前端登录页支持普通考生自助注册
- 注册成功后会自动登录，并以 `USER` 角色进入系统
- Java 启动时不再自动创建默认账号，账号统一以 SQL 初始化为准

## 数据库准备

1. 执行 [backend/db/init.sql](/C:/Users/杜路明/Desktop/project/backend/db/init.sql)
2. 按需修改 [application.yml](/C:/Users/杜路明/Desktop/project/backend/src/main/resources/application.yml) 中的 MySQL 连接信息

说明：

- 当前后端配置了 `spring.jpa.hibernate.ddl-auto=update`
- 首次启动后端时会自动补充示例题目、收藏、错题、练习记录与分类数据

## 启动方式

### 前端

```powershell
npm.cmd install
npm.cmd run dev
```

### 后端

```powershell
mvn spring-boot:run
```

## 访问地址

- 前端：[http://localhost:5173](http://localhost:5173)
- 后端：[http://localhost:8080](http://localhost:8080)
