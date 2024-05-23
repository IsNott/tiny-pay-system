# 简单的支付系统

自搭建的简单个人支付系统，预集成支付宝、微信等支付方式。

## 依赖

| name           |version|
|----------------|---|
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

## 使用
目前仅支持支付宝H5订单创建/支付，支付业务通知、退款接口。

退款业务通知处理还在开发中

- 交易

以支付宝H5为例，支付时调用交易网关接口
```
path:/transaction/gateway
body:{
    "paymentCode":"alipay",
    "paymentType":"h5",
    "subjectName":"cs",
    "amount":"0.01"
}
# 返回内容以code=200为成功
{
    "code": 200,
    "msg": "success",
    "obj": {
        "orderNo": "1243230959621373952",
         # 支付宝H5支付连接
        "pageData": "https://openapi-sandbox.dl.alipaydev.com/gateway.do?alipay_sdk=alipay-sdk-java-4.39.60.ALL&app_id=9021000122696227&biz_content=%7B%22out_trade_no%22%3A%2220240523100011243230961986961408%22%2C%22total_amount%22%3A%220.01%22%2C%22subject%22%3A%22cs%22%2C%22product_code%22%3A%22QUICK_WAP_WAY%22%7D&charset=UTF-8&format=json&method=alipay.trade.wap.pay&notify_url=http%3A%2F%2Fqvi7gj.natappfree.cc%2Fnotify%2Falipay&sign=RQSWnvFBgtaRMt7HZKfVAu8xUYDld%2Flj%W9xP5xGDOHNyExPfhJY%2FZ2Z8At2Pf1PN9qpekJEbCRmBrLS2x8poeVoAlsL3qkDi0jrrAMSUuo5XlSnKqK4Fsd%2ByTy9y7Nak7eLFVUGSU77vlsCFQ7xkbuI%2BcWUsbF4pD3c4Z7dbZXB5lN%2FHeFdhtJsi3LP5mVPgJUkt0iDeiBYeEnHSlro3yqm6eD4Xb5ANKxg%2FMyz%2BLkRrvABWaunKTwJEaSlIm9mZBbxtyv3CGqPlMm7q4CExlaX9deZCyLI4kQ%3D%3D&sign_type=RSA2&timestamp=2024-05-23+15%3A55%3A59&version=1.0"
    }
}
```

- 查单

- 退款

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

## 帮助
如果您恰巧熟悉支付系统，有更好的项目实践，
有兴趣想要作为项目贡献者，请提供PR，谢谢。

## Support
If you happen to be familiar with payment systems, 
have better project practices, and are interested in being a project contributor, please provide PR, thanks.