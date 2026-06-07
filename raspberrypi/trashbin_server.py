import bluetooth
import RPi.GPIO as GPIO
import time

SERVO_PIN = 18

GPIO.setmode(GPIO.BCM)
GPIO.setup(SERVO_PIN, GPIO.OUT)

servo = GPIO.PWM(SERVO_PIN, 50)
servo.start(0)


def set_angle(angle):
    duty = 2 + (angle / 18)
    GPIO.output(SERVO_PIN, True)
    servo.ChangeDutyCycle(duty)
    time.sleep(0.5)
    GPIO.output(SERVO_PIN, False)
    servo.ChangeDutyCycle(0)


def open_lid():
    print("open")
    set_angle(90)
    time.sleep(3)


def close_lid():
    print("close")
    set_angle(0)
    time.sleep(1)


server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
port = 1

server_sock.bind(("", port))
server_sock.listen(1)

bluetooth.advertise_service(
    server_sock,
    "TrashBinServer",
    service_classes=[bluetooth.SERIAL_PORT_CLASS],
    profiles=[bluetooth.SERIAL_PORT_PROFILE]
)

print("Connecting...")

client_sock, address = server_sock.accept()
print(f"Connected: {address}")

try:
    while True:
        data = client_sock.recv(1024).decode("utf-8").strip()

        if data:
            print(f"Data: {data}")

            if data == "O":
                open_lid()
                close_lid()

except Exception as e:
    print("error:", e)

finally:
    servo.stop()
    GPIO.cleanup()
    client_sock.close()
    server_sock.close()