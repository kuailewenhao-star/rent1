#!/bin/bash

# 房东租赁管理系统 - PC运营管理后台联调快速启动脚本
# 用法: ./scripts/start-integration-test.sh

echo "=========================================="
echo "房东租赁管理系统 - PC运营管理后台联调启动"
echo "=========================================="

# 1. 检查后端服务状态
echo "\n[1] 检查后端服务状态..."
BACKEND_URL="http://localhost:8080/api/health"
if curl -s -f "$BACKEND_URL" > /dev/null 2>&1; then
    echo "✓ 后端服务正常运行"
else
    echo "✗ 后端服务未启动，请先启动后端服务"
    echo "  启动命令: cd backend && mvn spring-boot:run"
    exit 1
fi

# 2. 检查数据库连接
echo "\n[2] 检查数据库连接..."
# 这里需要根据实际情况检查数据库连接

# 3. 启动前端开发服务器
echo "\n[3] 启动前端开发服务器..."
cd admin-web
npm run dev

echo "\n=========================================="
echo "联调环境已启动，请访问 http://localhost:3000"
echo "=========================================="