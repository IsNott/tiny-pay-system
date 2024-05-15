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
目前仅支持支付宝H5订单创建/支付，业务通知、退款还在开发中

1.创建订单，使用http进行接口调用，获取到orderNo后调用gateway网关接口
```
path:/transaction/createPay
body:{
    "subjectName":"cs", # 商品名称
    "amount":"0.01" # 金额，以CNY元为单位，方便前端展示
    }
# 返回内容以code=200为成功
response:{
    "code": 200,
    "msg": "success",
    "obj": {
        "orderNo": "1240240502364700672"
    }
}
```
2.创建订单后，调用交易网关接口
```
path:/transaction/gateway
body:{
    "orderNo": "1240240502364700672", # 订单号
    "paymentCode":"alipay", # 支付方式代码 支付宝-alipay
    "paymentType":"h5" # 支付业务方式
}
# 返回内容以code=200为成功
response:{
    "code": 200,
    "msg": "success",
    "obj": {
        "orderNo": "1240240502364700672",
        "pageData": "<form name=\"punchout_form\" method=\"post\" action=\"https://openapi-sandbox.dl.alipaydev.com/gateway.do?charset=UTF-8&method=alipay.trade.wap.pay&sign=MsOD46CPN8L7vkI%2BddldBHOC3Woulbgsrm7xhQhAIvIHHv%2F4zHXzMAfFgJIOd2xVaINhk9yY%2FF70QDd65AbU09uWpEFoAZhGNO%2BZVKz%2FkD03mblk1EhGtoeduV4MY9ugZZXT1YlETeOQ%2FGZc99lap5R0GgK%2Bgq4b88lICdDbof1YyYuN7wRYzcdMVQdVxRXotX05oHGUKsQwGJ8WzooSCKK%2B733SOQYRn47cTXYiQb3FYHAk7Qln7KyxJa%2B%2FKMl%2Bva9P3k39CEgVQCdwaKAsfMjZMg%3D%3D&version=1.0&app_id=9021000122696227&sign_type=RSA2&timestamp=2024-05-15+10%3A11%3A35&alipay_sdk=alipay-sdk-java-4.39.60.ALL&format=json\">\n<input type=\"hidden\" name=\"biz_content\" value=\"{&quot;out_trade_no&quot;:&quot;20240515100011240240504877088768&quot;,&quot;total_amount&quot;:&quot;0.01&quot;,&quot;subject&quot;:&quot;cs&quot;,&quot;product_code&quot;:&quot;QUICK_WAP_WAY&quot;}\">\n<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >\n</form>\n<script>document.forms[0].submit();</script>"
    }
}
```

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

| doing                    | todo                     | done                      |
|--------------------------| ------------------------ |---------------------------|
| \                        | \                        | 系统构建（基本交易接口定义、配置类、日志打印功能） |
| \                        | 支付宝H5支付通知处理逻辑 | 支付宝H5创建订单接口逻辑             |
| 交易入口根据标识注解分发到具体外部交易实现类逻辑 | \                        | \                         |
| \                        | \                        | gateway接口分发请求到具体外部交易实现类   |

## 帮助
如果您恰巧熟悉支付系统，有更好的项目实践，想要作为项目贡献者，请提供PR，谢谢。
