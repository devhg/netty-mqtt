#! /bin/bash
echo "INFO: 清理环境"
rm *.rsa
rm *.jks
rm *.p12
rm *.key
rm *.csr
rm *.srl

echo "INFO: 生成自签发证书"
openssl req -x509 -new -newkey rsa:2048 -nodes -keyout ca.key -out ca.pem -config ca-openssl.conf -days 3650 -extensions v3_req

echo "INFO: 将 ca.pem 转换为 ca.jks, KeyStore 密码为 123456"
keytool -importcert -trustcacerts -file ca.pem -keystore ca.jks -storepass 123456

echo "INFO: 签发客户端证书"
openssl genrsa -out client.key.rsa 2048
openssl pkcs8 -topk8 -in client.key.rsa -out client.key -nocrypt
openssl req -new -key client.key -out client.csr
openssl x509 -req -CA ca.pem -CAkey ca.key -CAcreateserial -in client.csr -out client.pem -days 3650

echo "INFO: 将私钥和对应的证书链合成 PKCS#12 格式，KeyStore 密码和私钥密码均为 123456"
openssl pkcs12 -export -CAfile ca.pem -in client.pem  -inkey client.key -out client.p12 -passout pass:123456

echo "INFO: 签发服务端证书"
echo "INFO: 填写主机名"
read -p "请输入服务器域名或者主机名：" server
echo "INFO: set alt_names $server"
old_server=$(grep "IP.1 = " server-openssl.conf|awk -F " " '{print $3}')
echo "INFO: 将 alt_names 从 $old_server 修改为 $server"
sed -i "s/$old_server/$server/g" server-openssl.conf
openssl genrsa -out server.key.rsa 2048
openssl pkcs8 -topk8 -in server.key.rsa -out server.key -nocrypt
openssl req -new -key server.key -out server.csr -config server-openssl.conf
openssl x509 -req -CA ca.pem -CAkey ca.key -CAcreateserial -in server.csr -out server.pem -extensions v3_req -extfile server-openssl.conf -days 3650

echo "INFO: 将私钥和对应的证书链合成 PKCS#12 格式，KeyStore 密码和私钥密码均为 123456"
openssl pkcs12 -export -CAfile ca.pem -in server.pem  -inkey server.key -out server.p12 -passout pass:123456

echo "INFO：清理无用的文件"
rm *.rsa
rm *.csr
rm ca.srl