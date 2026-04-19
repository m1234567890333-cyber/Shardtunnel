#!/bin/bash
# ShardTunnel Auto-Deploy 3.0 (KeyGen Edition)

echo -e "\033[0;32m[*] Начинаем установку ShardTunnel...\033[0m"

# 1. Генерация случайного ключа (32 символа)
SHARD_KEY=$(openssl rand -base64 32 | head -c 32)

# 2. Установка зависимостей
apt-get update -qq
apt-get install -y python3-pip iptables iptables-persistent openssl curl -y -qq

# 3. Установка криптографии
pip3 install cryptography --break-system-packages 2>/dev/null || pip3 install cryptography

# 4. Настройка NAT
IFACE=$(ip route get 8.8.8.8 | awk '{print $5; exit}')
sysctl -w net.ipv4.ip_forward=1 > /dev/null
iptables -t nat -A POSTROUTING -o $IFACE -j MASQUERADE
netfilter-persistent save > /dev/null 2>&1

# 5. Скачивание сервера
# ВАЖНО: убедись, что имя файла совпадает с тем, что на GitHub (shard-server.py)
curl -sLO https://raw.githubusercontent.com/m1234567890333-cyber/ShardTunnel/main/shard-server.py

# 6. Запуск сервера с новым ключом
# Используем nohup, чтобы сервер жил после закрытия консоли
pkill -f shard-server.py > /dev/null 2>&1
nohup python3 shard-server.py --port 443 --key "$SHARD_KEY" > vpn.log 2>&1 &

# 7. Финальный вывод
SERVER_IP=$(curl -s ifconfig.me)

echo -e "\n\033[0;32m==========================================="
echo -e "       SHARDTUNNEL УСПЕШНО ЗАПУЩЕН"
echo -e "===========================================\033[0m"
echo -e "IP Сервера: \033[1;37m$SERVER_IP\033[0m"
echo -e "Порт:       \033[1;37m443\033[0m"
echo -e "Ваш Ключ:   \033[1;33m$SHARD_KEY\033[0m"
echo -e "\033[0;32m===========================================\033[0m"
echo -e "Введите эти данные в Android-приложение."
echo -e "Логи работы: tail -f vpn.log"
