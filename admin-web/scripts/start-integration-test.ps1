# 房东租赁管理系统 - PC运营管理后台联调快速启动脚本 (Windows PowerShell)
# 用法: .\scripts\start-integration-test.ps1

Write-Host "=========================================="
Write-Host "房东租赁管理系统 - PC运营管理后台联调启动"
Write-Host "=========================================="

# 1. 检查后端服务状态
Write-Host "\n[1] 检查后端服务状态..."
$BACKEND_URL = "http://localhost:8080/api/health"
try {
    $response = Invoke-WebRequest -Uri $BACKEND_URL -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ 后端服务正常运行"
} catch {
    Write-Host "✗ 后端服务未启动，请先启动后端服务"
    Write-Host "  启动命令: cd backend; mvn spring-boot:run"
    exit 1
}

# 2. 启动前端开发服务器
Write-Host "\n[2] 启动前端开发服务器..."
Set-Location admin-web
npm run dev

Write-Host "\n=========================================="
Write-Host "联调环境已启动，请访问 http://localhost:3000"
Write-Host "=========================================="