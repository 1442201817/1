# 环境监测系统 (Env Monitor)

基于 Spring Boot + Vue.js 的环境监测数据管理平台，集成和风天气 API 和 DeepSeek AI 分析功能。

##  项目简介

本项目是一个环境监测数据管理系统，可以：
- 实时监测多个站点的环境数据（AQI、PM2.5、PM10、CO、温湿度等）
- 集成和风天气 API 获取实时天气信息
- 使用 DeepSeek AI 进行数据分析和建议
- 设置告警规则，超标自动预警
- 可视化数据展示（ECharts 图表）

##  技术栈

### 后端
- **框架**: Spring Boot 3.x
- **语言**: Java 21
- **数据库**: MySQL 8.0+
- **ORM**: JPA / Hibernate
- **AI 集成**: DeepSeek API

### 前端
- **框架**: Vue.js 3
- **UI 库**: Element Plus
- **HTTP 客户端**: Axios
- **图表库**: ECharts

##  环境要求

- JDK 21 或更高版本
- MySQL 8.0 或更高版本
- Maven 3.6+
- Node.js 16+（如需独立运行前端）

##  配置说明

### 1. MySQL 数据库配置

#### 创建数据库

执行 `database.sql` 文件创建数据库和表结构：

```bash
mysql -u root -p < database.sql
```

或者在 MySQL 客户端中手动执行 SQL 文件内容。

#### 修改数据库连接配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root          # 修改为你的 MySQL 用户名
    password: your_password # 修改为你的 MySQL 密码
```

**配置项说明：**
- `url`: 数据库连接地址，`data` 是数据库名称
- `username`: MySQL 用户名
- `password`: MySQL 密码
- `useSSL`: 是否使用 SSL 连接
- `serverTimezone`: 时区设置（建议设为 Asia/Shanghai）

### 2. 和风天气 API 配置

#### 获取 API Key

1. 访问 [和风天气开发者平台](https://dev.qweather.com/)
2. 注册账号并创建应用
3. 获取 API Key

#### 配置 API Key

编辑 `src/main/resources/application.yml`：

```yaml
qweather:
  api:
    key: your_qweather_api_key  # 替换为你的和风天气 API Key
    host: https://mr4d94gyqj.re.qweatherapi.com  # API 地址
```

**注意：**
- 免费版 API 有调用次数限制
- 确保 API Key 有效且有足够的配额
- `host` 地址根据你订阅的服务类型可能不同

### 3. DeepSeek AI API 配置

#### 获取 API Key

1. 访问 [DeepSeek 开放平台](https://platform.deepseek.com/)
2. 注册账号并创建 API Key
3. 充值或获取免费额度

#### 配置 API Key

编辑 `src/main/resources/application.yml`：

```yaml
ai:
  api:
    url: https://api.deepseek.com/chat/completions
    key: sk-your_deepseek_api_key  # 替换为你的 DeepSeek API Key
    model: deepseek-v4-flash       # 使用的模型名称
```

**配置项说明：**
- `url`: DeepSeek API 地址（通常不需要修改）
- `key`: 你的 DeepSeek API Key
- `model`: 使用的模型，可选值：
  - `deepseek-v4-flash`（推荐，速度快）
  - `deepseek-v4-pro`
  - 其他可用模型

##  快速开始

### 1. 克隆项目

```bash
git clone https://github.com/1442201817/1.git
cd env-monitor
```

### 2. 配置数据库

按照上述 MySQL 配置说明创建数据库并修改配置文件。

### 3. 配置 API Keys

按照上述说明配置和风天气和 DeepSeek API。

### 4. 编译项目

```bash
mvn clean package
```

### 5. 运行项目

#### 方式一：使用 Maven

```bash
mvn spring-boot:run
```

#### 方式二：使用启动脚本（Windows）

双击运行 `启动.bat` 文件

#### 方式三：运行 JAR 包

```bash
java -jar target/env-monitor-0.0.1-SNAPSHOT.jar
```

### 6. 访问应用

打开浏览器访问：http://localhost:8888

默认管理员账户：
- 用户名：admin
- 密码：admin123

## 📁 项目结构

```
env-monitor/
├── src/main/java/com/envmonitor/
│   ├── config/              # 配置类
│   │   └── DataInitializer.java
│   ├── controller/          # 控制器层
│   │   ├── AiController.java
│   │   ├── AlertRuleController.java
│   │   ├── AuthController.java
│   │   ├── EnvDataController.java
│   │   └── SysUserController.java
│   ├── entity/              # 实体类
│   │   ├── AlertRule.java
│   │   ├── EnvData.java
│   │   ├── MonitorStation.java
│   │   └── SysUser.java
│   ├── repository/          # 数据访问层
│   │   ├── AlertRuleRepository.java
│   │   ├── EnvDataRepository.java
│   │   ├── StationRepository.java
│   │   └── SysUserRepository.java
│   ├── service/             # 业务逻辑层
│   │   ├── AiService.java
│   │   ├── EnvDataService.java
│   │   └── QweatherService.java
│   └── EnvMonitorApplication.java
├── src/main/resources/
│   ├── static/              # 静态资源
│   │   ├── lib/             # 前端库
│   │   └── index.html       # 前端页面
│   └── application.yml      # 应用配置文件
├── database.sql             # 数据库建表脚本
├── pom.xml                  # Maven 配置文件
└── 启动.bat                 # Windows 启动脚本
```

##  数据库表结构

### 1. monitor_station（监测站点表）
- id: 站点ID
- name: 站点名称
- location: 站点位置
- lat/lng: 经纬度
- status: 站点状态（ONLINE/OFFLINE/MAINTENANCE）

### 2. env_data（环境数据表）
- id: 数据ID
- station_id: 关联站点ID
- aqi: 空气质量指数
- pm25/pm10: PM2.5/PM10 浓度
- co: 一氧化碳浓度
- temperature/humidity: 温度/湿度
- wind_speed/wind_direction: 风速/风向
- weather_description: 天气描述

### 3. alert_rule（告警规则表）
- id: 规则ID
- metric_name: 监控指标
- threshold: 阈值
- operator: 比较运算符
- level: 告警级别
- enabled: 是否启用

### 4. sys_user（系统用户表）
- id: 用户ID
- username: 用户名
- password: 密码（加密）
- name: 真实姓名
- role: 角色
- phone/email: 联系方式

##  API 接口

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 环境数据
- `GET /api/env-data/latest` - 获取最新数据
- `GET /api/env-data/history` - 获取历史数据
- `POST /api/env-data/sync` - 同步天气数据

### 告警规则
- `GET /api/alert-rules` - 获取告警规则列表
- `POST /api/alert-rules` - 创建告警规则
- `PUT /api/alert-rules/{id}` - 更新告警规则
- `DELETE /api/alert-rules/{id}` - 删除告警规则

### AI 分析
- `POST /api/ai/analyze` - AI 数据分析

### 用户管理
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户

**最后更新**: 2026-05-22
