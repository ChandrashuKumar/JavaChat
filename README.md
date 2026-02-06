## 1. Run the server file
Locally: 
```bash
javac -cp ".;mysql-connector-j-9.6.0.jar" ChatServer.java DatabaseHelper.java
java -cp ".;mysql-connector-j-9.6.0.jar" ChatServer
```
Or using docker:
```bash
docker-compose up --build
```

## 2. Run the client file 
```bash
javac ChatClient.java && java ChatClient [ip address of server]/localhost 6000
```
