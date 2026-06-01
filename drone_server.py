"""
AERO-RESCUE — Drone Telemetry WebSocket Server
================================================
SIH 2025 | PS ID 25047 | Disaster Response Drone for Remote Areas
Compatible with websockets v16+

Run: python drone_server.py
"""

import asyncio
import json
import math
import time
import websockets

# ── Drone Mission Coordinates ────────────────────────────────────────────────
BASE_LAT,   BASE_LNG   = 21.3122, 76.2261   # Drone take-off base (Burhanpur)
TARGET_LAT, TARGET_LNG = 21.3200, 76.2350   # Emergency target (nearby)

# ── Mission Parameters ───────────────────────────────────────────────────────
TOTAL_STEPS         = 300
UPDATE_INTERVAL_SEC = 0.5        # 2 updates per second
BATTERY_START       = 100
BATTERY_DRAIN_RATE  = 0.2

connected_clients = set()

# websockets v16+: handler takes only websocket (no path argument)
async def stream_drone(websocket):
    client_addr = websocket.remote_address
    connected_clients.add(websocket)
    print(f"\n[+] App connected from {client_addr}")
    print(f"    Total clients: {len(connected_clients)}")

    target_lat = TARGET_LAT
    target_lng = TARGET_LNG
    step = 0

    async def listen_for_commands():
        nonlocal target_lat, target_lng, step
        try:
            async for message in websocket:
                try:
                    cmd = json.loads(message)
                    if cmd.get("type") == "set_target":
                        new_lat = float(cmd["lat"])
                        new_lng = float(cmd["lng"])
                        if abs(target_lat - new_lat) > 0.00001 or abs(target_lng - new_lng) > 0.00001:
                            target_lat = new_lat
                            target_lng = new_lng
                            step = 0  # restart flight to new destination
                            print(f"\n    [📡] Mission target updated by App to: {target_lat:.5f}, {target_lng:.5f}")
                            print(f"         Resetting drone takeoff from base...")
                except Exception as e:
                    print(f"    [!] Command error: {e}")
        except Exception:
            pass

    listener_task = asyncio.create_task(listen_for_commands())

    try:
        while True:
            # Interpolate drone position
            t = min(step / TOTAL_STEPS, 1.0)
            lat = BASE_LAT + (target_lat - BASE_LAT) * t
            lng = BASE_LNG + (target_lng - BASE_LNG) * t

            # Realistic GPS noise
            lat += math.sin(step * 0.31) * 0.00008
            lng += math.cos(step * 0.27) * 0.00008

            battery      = max(5, BATTERY_START - int(step * BATTERY_DRAIN_RATE))
            altitude     = round(45.0 + math.sin(step * 0.15) * 8.0, 2)
            at_target    = t >= 1.0
            people_count = (14 if step > int(TOTAL_STEPS * 0.85)
                            else 7 if step > int(TOTAL_STEPS * 0.7)
                            else 0)
            payload_dropped = step > TOTAL_STEPS + 15
            status = ("payload_dropped" if payload_dropped
                      else "delivering" if at_target
                      else "en_route")

            data = {
                "lat":             round(lat, 7),
                "lng":             round(lng, 7),
                "battery":         battery,
                "altitude":        altitude,
                "people_count":    people_count,
                "status":          status,
                "payload_dropped": payload_dropped,
                "flight_mode":     "AUTO" if not at_target else "LOITER",
                "is_armed":        True,
                "voltage":         round(14.8 - (step * 0.01), 2),
                "timestamp":       int(time.time())
            }

            await websocket.send(json.dumps(data))

            if step % 10 == 0:
                print(f"   Step {step:>4} | {lat:.5f},{lng:.5f} | "
                      f"Batt {battery}% | People {people_count} | {status}")

            step += 1
            await asyncio.sleep(UPDATE_INTERVAL_SEC)

    except websockets.exceptions.ConnectionClosedOK:
        print(f"\n[x] App {client_addr} disconnected cleanly.")
    except websockets.exceptions.ConnectionClosedError as e:
        print(f"\n[!] Connection error: {e}")
    finally:
        listener_task.cancel()
        connected_clients.discard(websocket)
        print(f"    Remaining clients: {len(connected_clients)}")


def get_local_ip():
    import socket
    # 1. Try UDP routing method (Standard & highly reliable)
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        pass

    # 2. Fallback to hostname resolution (for completely offline local connections)
    try:
        hostname = socket.gethostname()
        ip = socket.gethostbyname(hostname)
        if ip and ip != "127.0.0.1":
            return ip
    except Exception:
        pass

    return "127.0.0.1"


async def main():
    HOST = "0.0.0.0"
    PORT = 8765
    ip = get_local_ip()

    print("=" * 55)
    print("  AERO-RESCUE Drone Telemetry Server")
    print("  SIH 2025 | PS ID 25047 | Govt of Odisha")
    print("=" * 55)
    print(f"  WebSocket: ws://{ip}:{PORT}")
    print(f"  Route: {BASE_LAT},{BASE_LNG} -> {TARGET_LAT},{TARGET_LNG}")
    print(f"  Update rate: {1/UPDATE_INTERVAL_SEC:.0f} Hz")
    print("  Waiting for Android app to connect ...")
    print("=" * 55)

    async with websockets.serve(stream_drone, HOST, PORT):
        await asyncio.Future()  # run forever


if __name__ == "__main__":
    asyncio.run(main())
