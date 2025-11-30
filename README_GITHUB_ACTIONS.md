# 🚀 GitHub Actions CI/CD

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

## Workflows

### `ci-cd.yml` - Full Pipeline
- Setup MySQL
- Build & Test
- Upload reports

**Trigger:** Push/PR vào `main` hoặc `develop`

### `simple-ci.yml` - Quick Test
- Basic test execution
- Scheduled runs (2h sáng UTC)

**Trigger:** Push/PR vào `main`, hoặc schedule

---

## View Results

1. **Actions** tab → Chọn workflow run
2. Scroll xuống **Artifacts**
3. Download `test-reports` để xem:
   - HTML reports
   - Screenshots
   - Logs

---

## Manual Trigger

1. **Actions** tab
2. Chọn workflow
3. **Run workflow** → Chọn branch → **Run workflow**

---

Xem chi tiết: [docs/CI_CD_SETUP.md](docs/CI_CD_SETUP.md)

