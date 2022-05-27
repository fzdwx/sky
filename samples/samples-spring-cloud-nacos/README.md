# samples-spring-cloud-nacos

主要测试是否能注册到nacos上

step 1. download nacos https://github.com/alibaba/nacos/releases

step 2. set your nacos related configuration

```yml
spring:
  cloud:
    nacos:
      discovery:
        username: nacos
        password: nacos
        server-addr: yrou addr
        group: DEFAULT_GROUP
        namespace: your namespace
```

成功注册：
![image](https://user-images.githubusercontent.com/65269574/170624882-b475aed2-bcbb-47c3-82b6-8fab1b53ebfa.png)
![image](https://user-images.githubusercontent.com/65269574/170624839-a638f74e-f6ed-40bf-9cad-31697ae5e110.png)