# 🚀 GitHub Actions CI/CD Demo

## Quick Start

1. **Push code lên GitHub**
   ```bash
   git push origin main
   ```

2. **Xem kết quả**
   - Vào tab **Actions** trên GitHub
   - Workflow tự động chạy

✅ **Xong!**

---

## Workflow

### `ci-cd-demo.yml` - Automation Test Pipeline

**Chạy:**
- Setup MySQL database
- Start Spring Boot app
- Run TestNG tests (Login & Register)
- Upload reports

**Trigger:** Push/PR vào `main` hoặc `develop`

---

## View Results

1. **Actions** tab → Chọn workflow run
2. Scroll xuống **Artifacts**
3. Download `test-reports` để xem:
   - Extent Reports HTML
   - Screenshots
   - Logs

---

## Manual Trigger

1. **Actions** tab
2. Chọn **GitHub Actions CI/CD Demo**
3. **Run workflow** → Chọn branch → **Run workflow**

---

Xem chi tiết: [docs/CI_CD_SETUP.md](docs/CI_CD_SETUP.md)
