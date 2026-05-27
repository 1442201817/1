-- ========================================
-- 环境监测系统数据库建表脚本
-- 数据库：MySQL
-- ========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS data DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE data;

-- ========================================
-- 1. 监测站点表 (monitor_station)
-- ========================================
CREATE TABLE IF NOT EXISTS monitor_station (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '站点ID',
    name VARCHAR(100) NOT NULL COMMENT '站点名称',
    location VARCHAR(255) COMMENT '站点位置',
    lat DOUBLE COMMENT '纬度',
    lng DOUBLE COMMENT '经度',
    status ENUM('ONLINE', 'OFFLINE', 'MAINTENANCE') DEFAULT 'ONLINE' COMMENT '站点状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监测站点表';

-- ========================================
-- 2. 环境数据表 (env_data)
-- ========================================
CREATE TABLE IF NOT EXISTS env_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据ID',
    station_id BIGINT COMMENT '站点ID（外键）',
    aqi INT COMMENT '空气质量指数',
    pm25 DOUBLE COMMENT 'PM2.5浓度（μg/m³）',
    pm10 DOUBLE COMMENT 'PM10浓度（μg/m³）',
    co DOUBLE COMMENT '一氧化碳浓度（mg/m³）',
    temperature DOUBLE COMMENT '温度（℃）',
    humidity DOUBLE COMMENT '湿度（%）',
    wind_speed DOUBLE COMMENT '风速（m/s）',
    wind_direction VARCHAR(50) COMMENT '风向',
    aqi_level VARCHAR(20) COMMENT 'AQI等级',
    weather_description VARCHAR(255) COMMENT '天气状况描述',
    recorded_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    FOREIGN KEY (station_id) REFERENCES monitor_station(id) ON DELETE SET NULL,
    INDEX idx_station_id (station_id),
    INDEX idx_recorded_at (recorded_at),
    INDEX idx_aqi (aqi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境监测数据表';

-- ========================================
-- 3. 告警规则表 (alert_rule)
-- ========================================
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    metric_name VARCHAR(50) NOT NULL COMMENT '指标名称（如：pm25, aqi, temperature等）',
    threshold DOUBLE NOT NULL COMMENT '阈值',
    operator VARCHAR(10) NOT NULL COMMENT '比较运算符（>, <, >=, <=, =）',
    level VARCHAR(20) COMMENT '告警级别（如：warning, critical）',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用（1-启用，0-禁用）',
    description VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_metric (metric_name),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- ========================================
-- 4. 系统用户表 (sys_user)
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（登录用）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(50) COMMENT '角色（如：admin, user）',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status VARCHAR(20) DEFAULT '正常' COMMENT '账号状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';
