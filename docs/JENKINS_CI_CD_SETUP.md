# 🚀 Jenkins CI/CD Setup Guide

## 📋 Tổng quan

Hệ thống CI/CD hoàn chỉnh với:
- ✅ Jenkins LTS (Docker)
- ✅ MySQL 8.0 (Docker)
- ✅ Selenium Grid Hub + Chrome Node (Docker)
- ✅ Maven Docker Agent cho builds
- ✅ Spring Boot Application
- ✅ Selenium TestNG Automation

---

## 🐳 A. Docker Compose Setup

### File: `docker-compose.yml`

```yaml
version: '3.9'

services:
  jenkins:
    image: jenkins/jenkins:lts
    container_name: jenkins
    privileged: true
    user: root
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - ./jenkins/jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      JAVA_OPTS: "-Djenkins.install.runSetupWizard=false -Xmx2048m"
    networks:
      - vegana-net
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    container_name: mysql8
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: vegana_store
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./vegana.sql:/docker-entrypoint-initdb.d/vegana.sql:ro
    networks:
      - vegana-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-p123456"]
      interval: 5s
      timeout: 5s
      retries: 10

  selenium-hub:
    image: selenium/hub:4.15.0
    container_name: selenium-hub
    ports:
      - "4444:4444"
    networks:
      - vegana-net
    restart: unless-stopped

  selenium-chrome:
    image: selenium/node-chrome:4.15.0
    container_name: selenium-chrome
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - HUB_HOST=selenium-hub
      - HUB_PORT=4444
    volumes:
      - /dev/shm:/dev/shm
    networks:
      - vegana-net
    restart: unless-stopped

volumes:
  mysql_data:
    driver: local

networks:
  vegana-net:
    driver: bridge
```

### Khởi động services

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f jenkins
```

---

## 🔧 B. Jenkinsfile

### File: `Jenkinsfile`

Pipeline sử dụng **Maven Docker Agent** với các đặc điểm:

- ✅ Agent: `maven:3.9.6-eclipse-temurin-21`
- ✅ Network: `--network vegana-net` (join vào cùng network)
- ✅ Cài `mysql-client` trong agent
- ✅ Connect MySQL qua `mysql:3306` (network nội bộ)
- ✅ Connect Selenium Hub qua `selenium-hub:4444` (network nội bộ)
- ✅ KHÔNG dùng `docker exec` (vì agent là container riêng)

**Các stages:**
1. Checkout code
2. Install MySQL client
3. Wait for MySQL
4. Setup database
5. Build application
6. Start Spring Boot
7. Check Selenium Grid
8. Run automation tests
9. Archive results

---

## ⚙️ C. Setup Instructions

### Bước 1: Start Docker Compose

```bash
# Start all services
docker-compose up -d

# Verify all containers are running
docker-compose ps
```

**Expected output:**
```
NAME              STATUS
jenkins           Up
mysql8            Up
selenium-hub      Up
selenium-chrome   Up
```

### Bước 2: Access Jenkins

1. Truy cập: `http://localhost:8080`
2. Lấy initial password:
   ```bash
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```

### Bước 3: Install Jenkins Plugins

Vào **Manage Jenkins** → **Manage Plugins** → **Available**, cài:

- ✅ **Pipeline**
- ✅ **Docker Pipeline**
- ✅ **TestNG Results**
- ✅ **AnsiColor**
- ✅ **GitHub Integration** (nếu dùng GitHub)

### Bước 4: Create Pipeline Job

1. **New Item** → Tên: `vegana-shop-ci-cd`
2. Chọn **Pipeline** → **OK**
3. Cấu hình:
   - **Pipeline definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/KhaiHASO/Vegana-Automation-Testing.git`
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`
4. **Save**

### Bước 5: Run Pipeline

Click **Build Now** để chạy pipeline.

---

## 🔍 D. Network Configuration

### Kiểm tra network

```bash
# List networks
docker network ls

# Inspect vegana-net
docker network inspect vegana-net

# Verify containers are in same network
docker network inspect vegana-net | grep -A 5 "Containers"
```

**Expected:** Tất cả containers (jenkins, mysql8, selenium-hub, selenium-chrome) đều trong `vegana-net`.

### Manual network connect (nếu cần)

```bash
# Nếu container không tự động join network
docker network connect vegana-net jenkins
docker network connect vegana-net mysql8
docker network connect vegana-net selenium-hub
docker network connect vegana-net selenium-chrome
```

---

## 📊 E. Pipeline Flow

```
1. Checkout Code
   ↓
2. Install MySQL Client (trong Maven agent)
   ↓
3. Wait for MySQL (mysqladmin ping via mysql:3306)
   ↓
4. Setup Database (CREATE DATABASE + import vegana.sql)
   ↓
5. Build Application (mvn clean package -DskipTests)
   ↓
6. Start Spring Boot (mvn spring-boot:run in background)
   ↓
7. Check Selenium Grid (curl selenium-hub:4444)
   ↓
8. Run Tests (mvn test với TestNG)
   ↓
9. Archive Results (Extent Reports, Screenshots, Surefire, Logs)
   ↓
10. Cleanup (stop app by PID)
```

---

## 🐛 F. Troubleshooting

### Lỗi: MySQL connection failed

```bash
# Check MySQL container
docker exec mysql8 mysqladmin ping -h localhost -uroot -p123456

# Check network
docker network inspect vegana-net

# Test connection từ Jenkins container
docker exec jenkins ping -c 2 mysql
```

### Lỗi: Selenium Grid not found

```bash
# Check Selenium Hub
curl http://localhost:4444/wd/hub/status

# Check network
docker exec selenium-hub ping -c 2 selenium-chrome
```

### Lỗi: Maven agent không cài được mysql-client

- Check logs trong Jenkins console output
- Verify apt-get có quyền trong container
- Có thể cần thêm `--privileged` flag (đã có trong docker-compose)

---

## ✅ G. Verification Checklist

- [ ] Docker Compose services đã start
- [ ] Tất cả containers trong cùng network `vegana-net`
- [ ] Jenkins accessible tại `http://localhost:8080`
- [ ] MySQL accessible từ Jenkins container
- [ ] Selenium Grid accessible tại `http://localhost:4444`
- [ ] Pipeline job đã tạo
- [ ] Jenkinsfile đã có trong repository
- [ ] Pipeline chạy thành công
- [ ] Test reports được archive

---

## 🚀 Quick Start Commands

```bash
# 1. Start everything
docker-compose up -d

# 2. Get Jenkins password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 3. Access Jenkins
open http://localhost:8080

# 4. Access Selenium Grid
open http://localhost:4444

# 5. Stop everything
docker-compose down

# 6. Stop and remove volumes
docker-compose down -v
```

---

**Happy CI/CD! 🎉**

