create database `rent_a_snow_hero` character set utf8mb4;
use rent_a_snow_hero;


-- 用户表
CREATE TABLE user
(
    user_id        BIGINT PRIMARY KEY COMMENT '主键',
    phone          VARCHAR(11) UNIQUE COMMENT '手机号',
    nickname       VARCHAR(50) COMMENT '昵称',
    avatar         VARCHAR(255) COMMENT '头像URL',
    gender         VARCHAR(2) COMMENT '性别(男/女)',
    province       VARCHAR(50) COMMENT '省',
    city           VARCHAR(50) COMMENT '市',
    district       VARCHAR(50) COMMENT '区',
    detail_address VARCHAR(255) COMMENT '详细地址',
    wechat_openid  VARCHAR(100) UNIQUE COMMENT '微信openid(唯一索引)',
    is_deleted     TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 管理员表
CREATE TABLE admin
(
    admin_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone        VARCHAR(11) UNIQUE COMMENT '手机号',
    password     VARCHAR(255) COMMENT '密码',
    is_deleted   TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '管理员表';

-- 装备类型表
CREATE TABLE equipment_type
(
    type_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_name    VARCHAR(20) UNIQUE COMMENT '类型名称(唯一索引)',
    is_deleted   TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '装备类型表';

-- 装备表
CREATE TABLE equipment
(
    equip_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_id        BIGINT COMMENT '外键',
    name           VARCHAR(50) COMMENT '名称',
    brand          VARCHAR(50) COMMENT '品牌',
    image_items    JSON COMMENT '图片列表',
    daily_price    DECIMAL(10, 2) COMMENT '日租金',
    deposit_amount DECIMAL(10, 2) COMMENT '固定押金',
    equip_price    DECIMAL(10, 2) COMMENT '装备价格',
    status         TINYINT  DEFAULT 0 COMMENT '状态(0正常/1维修)',
    description    TEXT COMMENT '装备描述',
    is_deleted     TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '装备表';

-- 租赁订单表
CREATE TABLE rental_order
(
    order_id          VARCHAR(32) PRIMARY KEY COMMENT '时间戳+随机数',
    user_id           BIGINT COMMENT '外键',
    snapshot_nickname VARCHAR(50) COMMENT '用户昵称快照',
    snapshot_address  VARCHAR(255) COMMENT '用户地址快照',
    use_time          DATETIME COMMENT '使用时间',
    start_time        DATETIME COMMENT '租赁开始时间',
    end_time          DATETIME COMMENT '预计归还时间',
    return_time       DATETIME COMMENT '实际归还时间',
    total_fee         DECIMAL(10, 2) COMMENT '总费用(租金+超时费)',
    deposit_fee       DECIMAL(10, 2) COMMENT '押金金额',
    rent_dates        JSON COMMENT '租赁日期集合',
    order_status      TINYINT COMMENT '订单状态(0-6)',
    delivery_method   TINYINT COMMENT '配送方式(0自提/1同城)',
    delivery_order_no VARCHAR(50) COMMENT '配送单号',
    equip_items       JSON COMMENT '装备列表',
    payment_no        VARCHAR(50) COMMENT '支付单号',
    is_deleted        TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '租赁订单表';

-- 购物车表
CREATE TABLE cart
(
    cart_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT COMMENT '外键',
    equip_items  JSON COMMENT '装备列表[1,2,3...]',
    rent_dates   JSON COMMENT '租赁日期集合',
    quantity     INT COMMENT '租赁数量',
    daily_price  DECIMAL(10, 2) COMMENT '日租金快照',
    is_deleted   TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '购物车表';

-- 库存日历表
CREATE TABLE equip_rental_calendar
(
    calendar_id  BIGINT PRIMARY KEY AUTO_INCREMENT,
    equip_id     BIGINT NOT NULL COMMENT '关联装备id',
    begin_date   DATE   NOT NULL COMMENT '开始日期',
    end_date     DATE   NOT NULL COMMENT '结束日期',
    is_deleted   TINYINT  DEFAULT 0 COMMENT '逻辑删除(0正常/1删除)',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '库存日历表';

-- 用户表索引
ALTER TABLE user
    ADD INDEX idx_user_phone (phone),
    ADD INDEX idx_wechat_openid (wechat_openid),
    ADD INDEX idx_created_time (created_time);

-- 管理员表索引
ALTER TABLE admin
    ADD INDEX idx_admin_phone (phone);

-- 装备类型表索引
ALTER TABLE equipment_type
    ADD INDEX idx_type_name (type_name);

-- 装备表索引
ALTER TABLE equipment
    ADD INDEX idx_equip_type (type_id),
    ADD INDEX idx_equip_status (status),
    ADD INDEX idx_equip_brand (brand);

-- 租赁订单表索引
ALTER TABLE rental_order
    ADD INDEX idx_order_user (user_id),
    ADD INDEX idx_order_status (order_status),
    ADD INDEX idx_payment_no (payment_no),
    ADD INDEX idx_created_time (created_time),
    ADD INDEX idx_delivery_method (delivery_method),
    ADD INDEX idx_start_end_time (start_time, end_time);

-- 购物车表索引
ALTER TABLE cart
    ADD INDEX idx_cart_user (user_id);

-- 库存日历表索引
ALTER TABLE equip_rental_calendar
    ADD INDEX idx_calendar_equip (equip_id),
    ADD INDEX idx_date_range (begin_date, end_date);

-- 针对订单列表页常见查询场景
ALTER TABLE rental_order
    ADD INDEX idx_status_user (order_status, user_id);

-- 针对装备管理列表常见筛选
ALTER TABLE equipment
    ADD INDEX idx_status_type (status, type_id);