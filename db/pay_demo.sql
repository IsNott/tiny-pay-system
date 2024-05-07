/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80016
 Source Host           : localhost:3306
 Source Schema         : pay_demo

 Target Server Type    : MySQL
 Target Server Version : 80016
 File Encoding         : 65001

 Date: 07/05/2024 08:51:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pay_order_info
-- ----------------------------
DROP TABLE IF EXISTS `pay_order_info`;
CREATE TABLE `pay_order_info`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `order_param` text CHARACTER SET utf8mb4  NULL COMMENT '订单内容',
  `payment_code` varchar(10) CHARACTER SET utf8mb4  NULL DEFAULT NULL COMMENT '支付方式code',
  `order_no` bigint(20) NOT NULL COMMENT '订单号',
  `order_type` tinyint(4) NOT NULL COMMENT '订单类型 1-支付 2-退款',
  `pay_status` tinyint(4) NULL DEFAULT NULL COMMENT '订单状态 0-init 1-paying 2-pay success 3-failed 4-refund',
  `in_transaction_no` bigint(20) NULL DEFAULT NULL COMMENT '内部交易号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

-- ----------------------------
-- Records of pay_order_info
-- ----------------------------

-- ----------------------------
-- Table structure for pay_payment_type
-- ----------------------------
DROP TABLE IF EXISTS `pay_payment_type`;
CREATE TABLE `pay_payment_type`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `payment_name` varchar(30) CHARACTER SET utf8mb4  NULL DEFAULT NULL COMMENT '支付方式名称',
  `payment_code` varchar(30) CHARACTER SET utf8mb4  NOT NULL COMMENT '支付方式代码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

-- ----------------------------
-- Records of pay_payment_type
-- ----------------------------

-- ----------------------------
-- Table structure for pay_transaction_info
-- ----------------------------
DROP TABLE IF EXISTS `pay_transaction_info`;
CREATE TABLE `pay_transaction_info`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `out_notify_msg` varchar(255) CHARACTER SET utf8mb4 NULL DEFAULT NULL COMMENT '外部回调信息',
  `out_transaction_no` varchar(255) CHARACTER SET utf8mb4  NULL DEFAULT NULL COMMENT '外部交易号',
  `out_transaction_param` text CHARACTER SET utf8mb4 NULL COMMENT '外部交易参数',
  `transaction_no` bigint(20) NOT NULL COMMENT '内部交易号',
  `transaction_status` tinyint(4) NULL DEFAULT NULL COMMENT '交易状态 订单状态 0-init 1-paying 2-pay success 3-failed 4-refund',
  `transaction_type` tinyint(4) NOT NULL COMMENT '交易类型 1-支付 2-退款',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

-- ----------------------------
-- Records of pay_transaction_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
