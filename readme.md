# 简单的支付系统

自搭建的简单个人支付系统，预集成支付宝、微信等支付方式。

## 依赖

| name           |version|
|----------------|--|
| Java           |17|
| springboot-web |3.2.1|
| mysql          |8+|
| mybatis-plus   ||

## 数据表
| 名称                   | 作用     |
|----------------------|--------|
| pay_order_info       | 内部订单记录 |
| pay_payment_type     | 定义的支付方式|
| pay_transaction_info | 外部交易记录|

## 开发记录
| date     |todo|
|----------|--|
| 2024/5/7 |系统构建、引入支付宝H5|