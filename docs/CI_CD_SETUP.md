# 🚀 GitHub Actions CI/CD Setup

## 📋 Quick Start

### Bước 1: Push code lên GitHub

```bash
git add .
git commit -m "Add CI/CD pipeline"
git push origin main
```

### Bước 2: Xem kết quả

1. Vào repository trên GitHub
2. Click tab **Actions**
3. Chọn workflow run để xem chi tiết

✅ **Xong!** Workflow tự động chạy khi có push/PR.

---

## 📁 Workflow Files

### 1. `ci-cd.yml` - Full Pipeline

**Tính năng:**
- ✅ Setup MySQL database
- ✅ Build project
- ✅ Start Spring Boot app
- ✅ Run automation tests
- ✅ Upload test reports

**Trigger:**
- Push vào `main` hoặc `develop`
- Pull Request
- Manual trigger (workflow_dispatch)

### 2. `simple-ci.yml` - Simple Pipeline

**Tính năng:**
- ✅ Basic test execution
- ✅ Upload reports
- ✅ Scheduled runs (2h sáng UTC)

**Sử dụng khi:**
- Cần pipeline đơn giản, nhanh
- Không cần nhiều browsers

---

## ⚙️ Configuration

### Database Settings

- **Host**: `localhost:3306`
- **Database**: `vegana_store`
- **Username**: `root`
- **Password**: `123456`

### Environment Variables

```yaml
BASE_URL: http://localhost:8080
BROWSER: chrome
DB_HOST: localhost
DB_PORT: 3306
DB_NAME: vegana_store
DB_USERNAME: root
DB_PASSWORD: 123456
```

---

## 🔍 Troubleshooting

### Workflow không chạy

**Kiểm tra:**
- File workflow có trong `.github/workflows/`?
- Syntax YAML đúng chưa?
- Branch trigger đúng chưa?

### MySQL connection failed

**Giải pháp:**
- Workflow tự động setup MySQL service
- Kiểm tra credentials trong workflow
- Xem logs trong Actions tab

### Application không start

**Kiểm tra:**
- MySQL đã sẵn sàng chưa (wait step)
- Port 8080 có conflict không
- Xem logs trong workflow run

### Tests fail

**Kiểm tra:**
- Download artifacts để xem reports
- Kiểm tra screenshots
- Xem test logs

---

## 📊 View Results

### Test Reports

1. Vào **Actions** tab
2. Chọn workflow run
3. Scroll xuống **Artifacts**
4. Download `test-reports` để xem:
   - HTML reports
   - Screenshots
   - Logs

### Manual Trigger

1. Vào **Actions** tab
2. Chọn workflow (ví dụ: CI/CD Pipeline)
3. Click **Run workflow**
4. Chọn branch và click **Run workflow**

---

## ✅ Checklist

- [ ] Workflow files đã có trong `.github/workflows/`
- [ ] Push code lên GitHub
- [ ] Workflow tự động chạy
- [ ] Tests chạy thành công
- [ ] Reports được upload

---

**Happy Testing! 🚀**
