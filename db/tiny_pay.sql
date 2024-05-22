/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50735 (5.7.35)
 Source Host           : localhost:3306
 Source Schema         : tiny_pay

 Target Server Type    : MySQL
 Target Server Version : 50735 (5.7.35)
 File Encoding         : 65001

 Date: 22/05/2024 16:14:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pay_order_info
-- ----------------------------
DROP TABLE IF EXISTS `pay_order_info`;
CREATE TABLE `pay_order_info`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `subject_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
  `amount` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '金额，以元为单位',
  `order_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '订单内容',
  `payment_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式code',
  `refund_order_no` bigint(20) NULL DEFAULT NULL COMMENT '退款订单号',
  `order_no` bigint(20) NOT NULL COMMENT '订单号',
  `order_type` tinyint(4) NOT NULL COMMENT '订单类型 1-支付 2-退款',
  `pay_status` tinyint(4) NULL DEFAULT 0 COMMENT '订单状态 0-init 1-paying 2-pay success 3-failed 4-refund',
  `in_transaction_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内部交易号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_payment_type
-- ----------------------------
DROP TABLE IF EXISTS `pay_payment_type`;
CREATE TABLE `pay_payment_type`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `payment_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式名称',
  `payment_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `payment_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式代码',
  `payment_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '第三方支付地址',
  `extra` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_transaction_info
-- ----------------------------
DROP TABLE IF EXISTS `pay_transaction_info`;
CREATE TABLE `pay_transaction_info`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `in_order_id` bigint(20) NULL DEFAULT NULL COMMENT '内部订单id',
  `out_notify_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '外部回调信息',
  `out_transaction_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外部交易号',
  `out_transaction_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '外部交易参数',
  `payment_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结算支付能力代码',
  `transaction_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内部交易号',
  `transaction_status` tinyint(4) NULL DEFAULT 0 COMMENT '交易状态 订单状态 0-init 1-paying 2-pay success 3-failed 4-refund',
  `transaction_type` tinyint(4) NOT NULL COMMENT '交易类型 1-支付 2-退款',
  `notify_time` datetime NULL DEFAULT NULL COMMENT '通知时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
