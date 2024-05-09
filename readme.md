# 简单的支付系统

自搭建的简单个人支付系统，预集成支付宝、微信等支付方式。

## 依赖

| name           |version|
|----------------|--|
| Java           |17|
| springboot-web |3.2.1|
| mysql          |8+|
| mybatis-plus   |3.5.5|

## 数据表
| 名称                   | 作用     |
|----------------------|--------|
| pay_order_info       | 内部订单记录 |
| pay_payment_type     | 定义的支付方式|
| pay_transaction_info | 外部交易记录|

## 目录结构

```
├─Application.java # 项目启动类
├─vo # vo对象
| └PayOrderInfoVo.java # 支付订单vo
├─service # 系统内部服务类
|    ├─H5PayService.java # 标识H5支付接口
|    ├─TransactionService.java # 标识交易接口
|    ├─impl # 服务实现类
|    |  ├─OrderService.java # 订单服务类
|    |  ├─PaymentService.java # 支付方式服务类
|    |  └TransactionService.java # 外部交易服务类
├─result # 交互结果类
|   ├─PayResult.java # 支付交互结果基类
|   ├─RefundResult.java # 退款交互结果基类
|   ├─alipay 
|   |   └AlipayH5Result.java # 支付宝H5支付交互结果
├─payment # 第三方支付实现类
|    ├─alipay
|    |   └AlipayService.java # 支付宝支付实现类
├─mapper # 数据访问层
|   ├─CommonMapper.java
|   ├─PayOrderInfoMapper.java
|   ├─PayPaymentTypeMapper.java
|   └PayTransactionInfoMapper.java
├─id # 流水号、id生成器
| ├─CustomIdGenerator.java # 自定义id生成，实现mybatisplus id生成接口
| ├─SnowflakeIdWorker.java # 雪花id生成器
| └TransactionNoFactory.java # 交易流水号生成器
├─exception # 异常包
|     ├─GlobalExceptionController.java  # 全局异常处理
|     └PayException.java # 自定义支付异常
├─enums # 枚举
|   ├─OrderTypeEnum.java # 订单类型枚举
|   ├─PayBusinessEnum.java # 支付业务枚举
|   └StatusEnum.java # 订单&交易状态枚举
├─entity # 实体
|   ├─PayOrderInfo.java # 订单对象
|   ├─PayPaymentType.java # 支付方式对象
|   └PayTransactionInfo.java # 外部交易对象
├─dto # 数据传输对象
|  ├─CreateOrderDTO.java # 创建订单dto
|  ├─PayOrderDTO.java # 支付订单dto
|  └RefundOrderDTO.java # 退款dto
├─controller # 控制层
|     ├─NotifyController.java # 交易通知
|     └TransactionController.java # 交易入口
├─config # 自定义配置包
|   ├─AlipayConfig.java # 支付宝配置
|   ├─AutoFillConfig.java # 自动填充字段配置
|   └WxPayConfig.java # 微信配置
├─common # 通用包（工具、统一结果类）
|   ├─CodeGenerator.java
|   ├─CommonUtils.java
|   ├─JacksonMapperBuilder.java
|   ├─R.java
|   ├─ReflectUtils.java
|   ├─SpringContextUtils.java
|   └ThreadPoolContext.java
├─aop # aop编程包
|  └LogAspect.java # aop环绕打印日志
├─annotations # 注解
|      ├─Payment.java # 标识第三方支付实现类注解
|      └PaymentType.java # 标识支付类型注解
```



## 开发记录

| doing                                            | todo                     | done                                               |
| ------------------------------------------------ | ------------------------ | -------------------------------------------------- |
| \                                                | \                        | 系统构建（基本交易接口定义、配置类、日志打印功能） |
| \                                                | 支付宝H5支付通知处理逻辑 | 支付宝H5创建订单接口逻辑                           |
| 交易入口根据标识注解分发到具体外部交易实现类逻辑 | \                        | \                                                  |

## 帮助
如果您恰巧是熟悉支付系统的相关概念，有更好的项目实践，想要作为项目贡献者，请提供PR，谢谢。