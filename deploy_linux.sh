
#!/bin/bash
# ============================================================
# 艾宾浩斯单词背诵学习系统 - Linux 服务器自动化部署脚本
# ============================================================
# 使用方式:
#   sudo bash deploy_linux.sh [选项]
# 
# 选项:
#   -p, --password    设置数据库密码（必填）
#   -a, --admin-password 设置初始管理员密码（必填，至少 12 位）
#   -s, --secret      设置 JWT Secret（可选，默认自动生成）
#   -P, --port        设置服务端口（可选，默认 8080）
#   -h, --help        显示帮助信息
# 
# 示例:
#   sudo bash deploy_linux.sh -p "MySecurePass123" -a "AdminSecurePass123"
#   sudo bash deploy_linux.sh -p "MyPass" -a "AdminSecurePass123" -P 8081
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 默认值
DB_PASSWORD=""
ADMIN_PASSWORD=""
JWT_SECRET=""
PORT=8080

# 解析参数
while [[ $# -gt 0 ]]; do
    case "$1" in
        -p|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        -s|--secret)
            JWT_SECRET="$2"
            shift 2
            ;;
        -a|--admin-password)
            ADMIN_PASSWORD="$2"
            shift 2
            ;;
        -P|--port)
            PORT="$2"
            shift 2
            ;;
        -h|--help)
            echo "使用方式: sudo bash deploy_linux.sh -p <数据库密码> -a <初始管理员密码> [-s <JWT密钥>] [-P <端口>]"
            exit 0
            ;;
        *)
            echo "未知参数: $1"
            exit 1
            ;;
    esac
done

# 检查密码参数
if [[ -z "$DB_PASSWORD" ]]; then
    echo -e "${RED}错误：必须使用 -p 参数指定数据库密码${NC}"
    echo "使用方式: sudo bash deploy_linux.sh -p <数据库密码>"
    exit 1
fi

if [[ ${#ADMIN_PASSWORD} -lt 12 ]]; then
    echo -e "${RED}错误：必须使用 -a 参数指定至少 12 位的初始管理员密码${NC}"
    exit 1
fi

# 检查是否以 root 用户运行
if [[ $EUID -ne 0 ]]; then
    echo -e "${RED}错误：请以 root 用户运行此脚本（使用 sudo）${NC}"
    exit 1
fi

# 生成随机 JWT Secret
generate_jwt_secret() {
    openssl rand -base64 32
}

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 步骤 1: 环境检查
echo -e "\n${CYAN}==========================================${NC}"
echo -e "${CYAN}艾宾浩斯单词背诵学习系统 - Linux 部署脚本${NC}"
echo -e "${CYAN}==========================================${NC}"

echo -e "\n${YELLOW}[步骤 1/5] 环境检查${NC}"

# 检查操作系统
OS=$(cat /etc/os-release | grep -E "^ID=" | cut -d= -f2 | tr -d '"')
echo -e "${GREEN}✓ 操作系统: $OS${NC}"

# 检查 Java
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F'"' '{print $2}')
    echo -e "${GREEN}✓ Java 版本: $JAVA_VERSION${NC}"
else
    echo -e "${RED}错误：未安装 Java${NC}"
    echo -e "${YELLOW}请安装 Java 17:${NC}"
    echo "  CentOS/RHEL: sudo yum install java-17-openjdk-devel -y"
    echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk -y"
    exit 1
fi

# 检查 Maven
if command_exists mvn; then
    echo -e "${GREEN}✓ Maven 已安装${NC}"
else
    echo -e "${RED}错误：部署会从当前源码构建，必须安装 Maven${NC}"
    exit 1
fi

# 检查 MySQL
if command_exists mysql; then
    echo -e "${GREEN}✓ MySQL 客户端已安装${NC}"
else
    echo -e "${RED}错误：MySQL 客户端未安装${NC}"
    exit 1
fi

# 检查 Redis
if command_exists redis-cli; then
    if redis-cli ping | grep -q PONG; then
        echo -e "${GREEN}✓ Redis 服务运行正常${NC}"
    else
        echo -e "${RED}错误：Redis 服务未运行${NC}"
        exit 1
    fi
else
    echo -e "${RED}错误：Redis 未安装${NC}"
    exit 1
fi

# 检查端口
if ss -tlnp | grep -q ":$PORT"; then
    echo -e "${RED}错误：端口 $PORT 已被占用${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 端口 $PORT 可用${NC}"

# 步骤 2: 数据库配置
echo -e "\n${YELLOW}[步骤 2/5] 数据库配置${NC}"

# 创建数据库和用户
echo -e "${YELLOW}正在创建数据库和用户...${NC}"
mysql -u root -e "
CREATE DATABASE IF NOT EXISTS ebbinghaus_vocab
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'vocab_user'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON ebbinghaus_vocab.* TO 'vocab_user'@'localhost';
FLUSH PRIVILEGES;
"

if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ 数据库和用户创建成功${NC}"
else
    echo -e "${RED}错误：数据库配置失败${NC}"
    exit 1
fi

# 步骤 3: 初始化数据库表
echo -e "\n${YELLOW}[步骤 3/5] 初始化数据库表${NC}"

SCHEMA_PATH="backend/src/main/resources/db/schema.sql"
if [[ ! -f "$SCHEMA_PATH" ]]; then
    echo -e "${RED}错误：未找到 schema.sql 文件${NC}"
    exit 1
fi

echo -e "${YELLOW}正在执行数据库初始化脚本...${NC}"
mysql -u vocab_user -p"$DB_PASSWORD" ebbinghaus_vocab < "$SCHEMA_PATH"

if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ 数据库表初始化成功${NC}"
else
    echo -e "${RED}错误：数据库初始化失败${NC}"
    exit 1
fi

# 步骤 4: 配置应用
echo -e "\n${YELLOW}[步骤 4/5] 配置应用${NC}"

# 生成 JWT Secret
if [[ -z "$JWT_SECRET" ]]; then
    JWT_SECRET=$(generate_jwt_secret)
    echo -e "${YELLOW}生成新的 JWT Secret...${NC}"
fi

# 将敏感信息放入仅 root 可读的 systemd 环境文件，不写入源码或 JAR。
escape_env_value() {
    local value="$1"
    value="${value//\\/\\\\}"
    value="${value//\"/\\\"}"
    printf '"%s"' "$value"
}

ENV_FILE="/etc/vocab.env"
umask 077
{
    echo "DB_USERNAME=vocab_user"
    printf 'DB_PASSWORD=%s\n' "$(escape_env_value "$DB_PASSWORD")"
    printf 'JWT_SECRET=%s\n' "$(escape_env_value "$JWT_SECRET")"
    echo "SERVER_PORT=$PORT"
    echo "ADMIN_BOOTSTRAP_ENABLED=true"
    echo "ADMIN_BOOTSTRAP_USERNAME=admin"
    printf 'ADMIN_BOOTSTRAP_PASSWORD=%s\n' "$(escape_env_value "$ADMIN_PASSWORD")"
} > "$ENV_FILE"
chmod 600 "$ENV_FILE"

echo -e "${GREEN}✓ 安全运行配置已写入 $ENV_FILE${NC}"

# 步骤 5: 打包与启动服务
echo -e "\n${YELLOW}[步骤 5/5] 打包与启动服务${NC}"

# 始终从当前源码重新构建，避免部署过期 JAR。
JAR_PATH="backend/target/vocabulary-learning-system-0.0.1-SNAPSHOT.jar"
if ! command_exists mvn; then
    echo -e "${RED}错误：需要 Maven 来构建项目${NC}"
    exit 1
fi

cd backend
if mvn clean package -DskipTests; then
    echo -e "${GREEN}✓ 项目构建成功${NC}"
else
    echo -e "${RED}错误：项目构建失败${NC}"
    exit 1
fi
cd ..

# 创建日志目录
mkdir -p logs

# 创建 systemd 服务文件
SERVICE_FILE="/etc/systemd/system/vocab.service"
echo -e "${YELLOW}创建 systemd 服务文件...${NC}"

cat > "$SERVICE_FILE" << EOF
[Unit]
Description=Ebbinghaus Vocabulary Learning System
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$(pwd)/backend
EnvironmentFile=/etc/vocab.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar target/vocabulary-learning-system-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=append:$(pwd)/logs/stdout.log
StandardError=append:$(pwd)/logs/stderr.log

[Install]
WantedBy=multi-user.target
EOF

# 重新加载 systemd 并启动服务
echo -e "${YELLOW}启动服务...${NC}"
systemctl daemon-reload
systemctl enable vocab
systemctl start vocab

# 等待服务启动
sleep 5

# 检查服务状态
if systemctl is-active --quiet vocab; then
    echo -e "${GREEN}✓ 服务启动成功${NC}"
else
    echo -e "${RED}错误：服务启动失败${NC}"
    echo -e "${YELLOW}查看错误日志: tail -20 $(pwd)/logs/stderr.log${NC}"
    exit 1
fi

# 验证服务
echo -e "\n${YELLOW}[验证] 正在检查服务状态...${NC}"
sleep 3

RESPONSE=$(curl -s "http://localhost:$PORT/api/health")
if echo "$RESPONSE" | grep -q '"code":200'; then
    echo -e "${GREEN}✓ 服务健康检查通过${NC}"
    
    echo -e "\n${CYAN}==========================================${NC}"
    echo -e "${CYAN}部署完成！${NC}"
    echo -e "${CYAN}==========================================${NC}"
    echo -e "\n${GREEN}服务地址：http://localhost:$PORT/api${NC}"
    echo -e "${GREEN}健康检查：http://localhost:$PORT/api/health${NC}"
    echo -e "${GREEN}API文档：http://localhost:$PORT/api/doc.html${NC}"
    echo -e "\n${YELLOW}配置信息：${NC}"
    echo -e "${YELLOW}  - 数据库用户：vocab_user${NC}"
    echo -e "${YELLOW}  - 敏感配置文件：/etc/vocab.env（仅 root 可读）${NC}"
    echo -e "\n${CYAN}==========================================${NC}"
    echo -e "\n${YELLOW}服务管理命令：${NC}"
    echo -e "${YELLOW}  启动：systemctl start vocab${NC}"
    echo -e "${YELLOW}  停止：systemctl stop vocab${NC}"
    echo -e "${YELLOW}  重启：systemctl restart vocab${NC}"
    echo -e "${YELLOW}  状态：systemctl status vocab${NC}"
    echo -e "${YELLOW}  日志：journalctl -u vocab -f${NC}"
else
    echo -e "${RED}错误：服务状态异常${NC}"
    echo -e "${YELLOW}响应：$RESPONSE${NC}"
    exit 1
fi

exit 0
