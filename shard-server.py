import socket
import json
import argparse
import struct
import time
from cryptography.hazmat.primitives.ciphers.aead import ChaCha20Poly1305

def start_server():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=443)
    parser.add_argument("--key", type=str, default="shard_tunnel_ultimate_key_32bytes")
    args = parser.parse_args()

    # Ключ строго 32 байта
    cipher = ChaCha20Poly1305(args.key.encode().ljust(32)[:32])
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(('0.0.0.0', args.port))
    print(f"[*] ShardTunnel запущен на порту {args.port}")

    while True:
        try:
            data, addr = sock.recvfrom(2048)
            # Срезаем маскировку (64 байта) и берем Nonce (12 байт)
            if len(data) < 76: continue
            
            nonce = data[64:76]
            encrypted = data[76:]
            
            # Расшифровка
            decrypted = cipher.decrypt(nonce, encrypted, None)
            
            # Проверка Timestamp (защита от повторов)
            p_time = struct.unpack("!Q", decrypted[:8])[0]
            if abs(int(time.time()) - p_time) > 60: continue

            # Здесь пакет уходит в систему через TUN/NAT
        except:
            continue # Silent Mode: игнорируем любые ошибки

if __name__ == "__main__":
    start_server()
