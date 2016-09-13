# dao.tephra.config.properties
```properties
## 设置数据库IP地址+端口号集。
## 使用JSON数据格式，每个设置对象包含key与values属性，其中
## * key 为数据库引用名称，空则为默认数据库；必须有且仅有一个key设置为空。
## * type 数据库方言，可选：mysql、oracle。
## * username 数据库登入用户名。
## * password 数据库登入密码。
## * ips 指定数据库的访问IP地址+":"+端口号，如果设置多个值则第一个为可读写库，其余均为只读库。
## * schema 数据库名。
#tephra.dao.database.config = [{key:"",type:"mysql",username:"root",password:"root",ips:["127.0.0.1:3306"],schema:"d_tephra"}]
## 连接池初始化连接数量
#tephra.dao.database.initial-size = 0
## 连接池最大激活数量，如果非正整数，则不做限制。
#tephra.dao.database.max-active = 5
## 连接池连接最大等待时间（单位毫秒）， -1 则将无限等待。
#tephra.dao.database.max-wait = 5000
## 连接检测间隔时间（单位：毫秒），0表示不检测。
#tephra.dao.database.test-interval = 600000
## 设置自动断开连接最大时长，单位：秒。
#tephra.dao.database.remove-abandoned-timeout = 300

## Hibernate Orm设置。
## 是否允许Hibernate显示SQL语句。
#tephra.dao.orm.hibernate.show-sql = false
## 设置Model搜索包名集。
## 使用JSON数据格式，每个设置对象包含key与values属性，其中
## * key 为数据库引用名称；空则为默认库；必须有且仅有一个key设置为空。
## * values 为搜索包名集。
#tephra.dao.orm.hibernate.packages-to-scan = [{key:"",values:["org.lpw.tephra"]}]

## MyBatis Orm设置。
## 设置Model搜索包名集。
## 使用JSON数据格式，每个设置对象包含key与values属性，其中
## * key 为数据库引用名称；空则为默认库；必须有且仅有一个key设置为空。
## * values 为搜索包名集。
#tephra.dao.orm.mybatis.mappers = [{key:"",values:["org.lpw.tephra"]}]

## 设置数据库IP地址+端口号集。
## 使用JSON数据格式，每个设置对象包含key与values属性，其中
## * key 为数据库引用名称，空则为默认数据库；必须有且仅有一个key设置为空。
## * username 数据库登入用户名。
## * password 数据库登入密码。
## * ips 指定数据库的访问IP地址+":"+端口号，如果设置多个值则自动进行负载均衡。
## * schema 数据库名。
#tephra.dao.mongo.config = [{key:"",username:"root",password:"root",ips:["127.0.0.1:27017"],schema:"d_tephra"}]
```