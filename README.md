## 1. Run the server file 
```bash
javac -cp ".;mysql-connector-j-9.6.0.jar" ChatServer.java DatabaseHelper.java
java -cp ".;mysql-connector-j-9.6.0.jar" ChatServer
```
## 2. Run the client file 
```bash
javac ChatClient.java && java ChatClient [ip address of server] 6000
```
