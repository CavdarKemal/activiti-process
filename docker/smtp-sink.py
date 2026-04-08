"""Minimaler SMTP-Sink: akzeptiert alle Mails und verwirft sie."""
import socket
import threading

def handle_client(conn, addr):
    try:
        conn.sendall(b"220 localhost SMTP Sink\r\n")
        while True:
            data = conn.recv(1024)
            if not data:
                break
            line = data.decode("utf-8", errors="replace").strip().upper()
            if line.startswith("EHLO") or line.startswith("HELO"):
                conn.sendall(b"250 OK\r\n")
            elif line.startswith("MAIL FROM"):
                conn.sendall(b"250 OK\r\n")
            elif line.startswith("RCPT TO"):
                conn.sendall(b"250 OK\r\n")
            elif line.startswith("DATA"):
                conn.sendall(b"354 Send data\r\n")
                while True:
                    data = conn.recv(4096)
                    if not data or data.strip().endswith(b"."):
                        break
                conn.sendall(b"250 OK\r\n")
            elif line.startswith("QUIT"):
                conn.sendall(b"221 Bye\r\n")
                break
            else:
                conn.sendall(b"250 OK\r\n")
    except Exception:
        pass
    finally:
        conn.close()

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(("0.0.0.0", 25))
sock.listen(5)
print("SMTP-Sink gestartet auf Port 25", flush=True)
while True:
    conn, addr = sock.accept()
    threading.Thread(target=handle_client, args=(conn, addr), daemon=True).start()
