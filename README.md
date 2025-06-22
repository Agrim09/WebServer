# 🔐 Java TLS Secure Client-Server Application

This project demonstrates a secure client-server architecture using **TLS (SSL)** in Java. It supports secure communication, configurable logging, multi-threaded server handling, and externalized configurations.

## 📌 Features

- TLS/SSL-based secure communication
- Multi-threaded server handling with a fixed thread pool
- Configurable via `server.properties` and `client.properties`
- Java Logging with `client.log` and `server.log` output
- Proper TLS handshake and certificate-based security
- Minimal command-response communication protocol

## 🧱 Tech Stack

- Java SE 8+
- TLS/SSL (via Java Secure Socket Extension - JSSE)
- Java Logging API (`java.util.logging`)
- Properties-based configuration
- Java KeyStore (JKS) for certificates

---

## 🚀 Getting Started

### 📁 Directory Structure

<pre>
.
├── Server.java
├── Client.java
├── server.properties
├── client.properties
├── server.log
├── client.log
├── server.keystore
├── client.truststore
</pre>

### 🔧 Configuration

#### `server.properties`
port=8443
keystore.path=server.keystore
keystore.password=password
log.level=INFO

#### `client.properties`
server.host=localhost
server.port=8443
command=HELLO
log.level=INFO
