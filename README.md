# 开发规范

## 工程结构

1. 【推荐】模块包结构如下；

```
.
├── /src/                      # 项目源码目录
│ ├── /main/     			   # 主要代码目录
│ │ ├── /java/                 # 应用布局
│ │ ├── /package.name/         # 模块包名
│ │ │ ├── /config/             # 定义模块的默认配置
│ │ │ │ ├── /domain/           # 定义@Entity,与数据库映射关系
│ │ │ │ ├── /repository/       # 基于JPA定义数据访问接口，依赖domain层
│ │ │ │ ├── /service/          # 定义业务逻辑，事务在此层统一控制，依赖repository层
│ │ │ │ │ ├── /event/          # 定义服务层使用的事件
│ │ │ │ │ │ ├── /listener/     # 定义异步事件监听
│ │ │ │ ├── /web/              # web相关的内容，依赖service层
│ │ │ │ │ ├── /api/            # 定义模块对外的API接口
│ │ ├── /resources/            # 资源文件目录
│ │ │ └── bootstrap.yml        # 默认配置文件
│ ├── /test/                   # 测试代码目录
│ │ ├── /java/                 # java测试代码
│ │ ├── /resources/            # 测试资源文件目录
```

- **domain**：定义领域模型和对应的数据库存储映射、查询条件构造器criteria；不依赖其他层。
- **repository**：定义数据库访问接口；依赖domain。
- **service**：定义服务的具体实现；依赖repository和domain。
- **web/api**：定义rest接口；依赖service和domain 。

2. 【**强制**】事务标记 `@Transactional` 只能出现在service层，统一在service层的方法中显式声明；并在方法上注解`@Transactional`其他层不进行事务控制；如果需要控制service的事务，可以创建一个 `XxxManager` 类，在方法中显示声明事务后调用其他service的方法；
3. 【**强制**】domain中用到的枚举直接定义在对应的模型中。
4. 【推荐】domain中的数据模型可以直接作为REST接口返回给用户，所以应该按需定义模型，不需要和数据库字段一一对应。一张表可以映射多个模型，同样一个模型也可以映射多张表。

## redis规约

### 命名

1. 【**强制**】key统一使用小写字母，单词间用下划线隔开，力求语义表达完整清楚，不要嫌名字长

2. 【**强制**】key每个模块使用自己的前缀

>   **正例**： oauth2:access:160c8ee0-62c2-4bd9-9388-f8ec7aee75f5
>
>   *反例：OTC:CustomerReal:0100100000026*




## REST接口规约

### 命名

1. 【**强制**】**RUL**地址和**参数**统一使用小写字母，单词间用下划线隔开，力求语义表达完整清楚，不要嫌名字长。

>   - **正例**：/api/org/user?user_name=xxx
>   - *反例：/api/org/User/findByUserName?userName=xxx*

2. 【**强制**】**返回的结果**直接使用 lowerCamelCase 风格，必须遵驼峰形式。 

>  - **正例**： id/ version / createdBy / createdTime /lastModifiedBy /lastModifiedTime
>

3. 【**强制**】URL带上模块的前缀。

>   - **正例**： /api/organization/user
>   - *反例：/api/user*

4. 【**强制**】不需要**身份认证**的 `api` 地址添加 `/public/` 标志。

>   - **正例**： /api/organization/public/user/current
>   - *反例*：/api/organization/user/current

5. 【推荐】URL地址上不出现delete,update,create,get等字样，可以通过对应的请求方法DELETE PUT POST GET等请求方法动词代替。

>   - **正例**： /api/org/user
>   - *反例：/api/org/get_user*

5. 【推荐】分页请求参数，?page=第几页(从0开始计数)&size=每页的最大记录数


### 返回结果

1. 【**强制**】返回HTTP状态码，按照http的标准状态码返回，比如2XX / 3XX /4XX /5XX；参考`org.springframework.http.HttpStatus`

2. 【**强制**】返回的时间格式统一为：yyyy-MM-dd HH:mm:ss

3. 【**强制**】返回错误消息，统一返回英文标识的错误信息，小写字母，单词间用下划线隔开。前端根据message进行翻译

  **正例**：

  ```sh
  curl https://api.xxx.com/user
  
  HTTP/1.1 500 Internal Server Error
  Content-Type: application/json; charset=utf-8
  {
      message: "error_org_user_not_found" //具体错误消息
      args:["arg1", "arg2", "arg3"]       //显示消息需要的参数，比如订单号等
  }
  ```

- 返回一个对象

```sh
curl https://api.xxx.com/user/{id}

HTTP/1.1 200 ok
Content-Type: application/json; charset=utf-8
{
    id: "xxxx", 
    version: 1,
    createdBy: "creator",
    createdTime: "yyyy-MM-dd HH:mm:ss" 
}
```

- 返回列表

```sh
  curl  https://api.xxx.com/user
  
  HTTP/1.1 200 ok
  Content-Type: application/json; charset=utf-8
  [
       {id: "xxxx", version: 1, createdBy: "creator", createdTime: "yyyy-MM-dd HH:mm:ss"},
       {id: "xxxx", version: 1, createdBy: "creator", createdTime: "yyyy-MM-dd HH:mm:ss"}
  ]
```

- 分页返回列表

```sh
curl  https://api.xxx.com/user?page=1&size=10

HTTP/1.1 200 ok
Content-Type: application/json; charset=utf-8
{
    size: 10 //每页最大记录数
    numberOfElements: 9 //实际返回的记录数
    totalPages: 2 //总页数
    totalElements: 19 //总记录数
    number: 2 //当前页数，从0开始计数
    content: [
    {id: "xxxx", version: 1, createdBy: "creator", createdTime: "yyyy-MM-dd HH:mm:ss"},
    {id: "xxxx", version: 1, createdBy: "creator", createdTime: "yyyy-MM-dd HH:mm:ss"}
    ...
    ]
}
```



## 签名规约

### 请求头信息

```
Content-Type: application/json;charset=UTF-8
X-API-Version: 1.0.0
X-API-Key: client_id //分配的client_id
X-API-Timestamp: 2018-07-18T01:25:47.048Z //请求时间戳(ISO)
X-API-Nonce: md5(client_id+timestamp+seqNum++) //客户端随机数，seqNum为客户端请求随机数,客户端自己保证唯一
//方法参数签名相关
X-API-Signature-Params: arg1,arg2 //需要签名的参数，使用分号分隔；
X-API-Signature-Method：HmacMD5|HmacSHA1|HmacSHA256 //签名方法，默认HmacSHA256
X-API-Signature: sign_method(client_secret,params + version + nonce + timestamp)//方法参数签名；
```

### 签名步骤

参数必须按照X-API-Signature-Params的顺序发送，步骤如下：

```
1. 将请求参数按照X-API-Signature-Params的顺序排序(结果为：参数1,参数2[,参数3....]) 
2. 遍历步骤1排序完的结果, 将参数值拼接上去(结果为：参数1=参数值1&参数2=参数值2[,参数3=参数3值.....])
3. 将步骤2得到的结果值后面拼接上签名规范的版本、生成的随机数、接口的路径(结果为：参数1=参数值1&参数2=参数值2[,参数3=参数3值.....]#version#nonce#url)
4. 将步骤3得到的结果和分配的client_secret按照签名方法（HmacMD5|HmacSHA1|HmacSHA256）签名sign(client_secret,params + version + nonce + url ), 得到最终的签名值
5. access_token 为“获取access_token”获得的返回参数access_token
```


### 示例

以请求接口 `api/entrust/exchange/history/user` 为例子：

**URL：** `/api/entrust/exchange/history/user`

**参数：**`page=0,size=2`

**请求时间:**  `2019-01-18T14:16:019+0800`

**ClientId：** `e65c869659ce4037bb19d80441e00000`
**ClientSecret:** `1d98a1386d034354b8f030bf2bc00000`

```sh
1、指定签名参数已经顺序
X-API-Signature-Params=page,size
2、构造参数
page=0&size=2
3、构造要签名的字符串
page=0&size=111.0.033711d22c5410817e7637611f36bd91b/api/entrust/exchange/history/user
4、使用HmacSHA256签名参数，签名使用的key为上面获取到的{Secret Key}
HmacSHA256(1d98a1386d034354b8f030bf2bc00000, page=0&size=111.0.033711d22c5410817e7637611f36bd91b/api/entrust/exchange/history/user)=3fab7dc10e193d3e2753a5f92890a8f73598c8bc7155444697cd8ce8659864cf

//以下是每个头信息的计算过程：
//指定版本
X-API-Version=1.0.0
//指定请求时间戳
X-API-Timestamp=2019-01-18T14:11:046+0800
//计算X-API-Nonce
X-API-Nonce=md5(e65c869659ce4037bb19d80441e000002019-01-18T14:16:019+08000) = 33711d22c5410817e7637611f36bd91b
//指定签名参数已经顺序
X-API-Signature-Params=page,size
//参数签名
X-API-Signature=HmacSHA256(1d98a1386d034354b8f030bf2bc00000, page=0&size=111.0.033711d22c5410817e7637611f36bd91b/api/entrust/exchange/history/user)=3fab7dc10e193d3e2753a5f92890a8f73598c8bc7155444697cd8ce8659864cf

//Request Header:
Authorization:Bearer f88de959-6015-4f12-85d0-a330eecefc9b
Content-Type:application/x-www-form-urlencoded;charset=UTF-8

X-API-Key: e65c869659ce4037bb19d80441e00000
X-API-Version: 1.0.0
X-API-Timestamp: 2019-01-18T14:16:019+0800
X-API-Nonce: 33711d22c5410817e7637611f36bd91b
X-API-Signature-Params: page,size
X-API-Signature-Method: HmacSHA256
X-API-Signature: 3fab7dc10e193d3e2753a5f92890a8f73598c8bc7155444697cd8ce8659864cf

```