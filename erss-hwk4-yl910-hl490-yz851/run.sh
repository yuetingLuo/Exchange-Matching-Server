#!/bin/bash

# 清理之前的构建结果，编译并打包项目
mvn clean install

# 判断Maven构建是否成功
if [ $? -eq 0 ]; then
    echo "Maven build was successful. Running the application..."
    # 运行编译后的jar文件
    java -jar target/stocktrading-1.0-SNAPSHOT.jar
else
    echo "Maven build failed. Please check the error messages."
fi
