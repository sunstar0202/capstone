import bluetooth
import RPi.GPIO as GPIO
import time

PET_SERVO = 18
CAN_SERVO = 19
GLASS_SERVO = 20

GPIO.setmode(GPIO.BCM)

GPIO.setup(PET_SERVO, GPIO.OUT)
GPIO.setup(CAN_SERVO, GPIO.OUT)
GPIO.setup(GLASS_SERVO, GPIO.OUT)

pet_pwm = GPIO.PWM(PET_SERVO, 50)
can_pwm = GPIO.PWM(CAN_SERVO, 50)
glass_pwm = GPIO.PWM(GLASS_SERVO, 50)

pet_pwm.start(0)
can_pwm.start(0)
glass_pwm.start(0)

#모터제어

def servo_angle(pwm, angle):
    duty = 2.5 + angle / 18.0

    pwm.ChangeDutyCycle(duty)
    time.sleep(0.5)

    pwm.ChangeDutyCycle(0)

def servo_open(pwm):
    servo_angle(pwm, 90)

def servo_close(pwm):
    servo_angle(pwm, 0)


#플라스틱, 페트병
def open_pet():

    print("PET 통 개방")

    servo_open(pet_pwm)

    time.sleep(2)

    servo_close(pet_pwm)


#캔
def open_can():

    print("CAN 통 개방")

    servo_open(can_pwm)

    time.sleep(2)

    servo_close(can_pwm)


#유리

def open_glass():

    print("GLASS 통 개방")

    servo_open(glass_pwm)

    time.sleep(2)
    servo_close(glass_pwm)





#ㅡㅡㅡ



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



try:
    while True:
        data = client_sock.recv(1024).decode().strip()

        if not data:
            continue
        
        print(f"Data: {data}")

        if data == "P":
            open_pet()

        elif data == "C":
            open_can()

        elif data == "G":
            open_glass()

        elif data == "X":

            print("오염도 초과 - 투입 거부")

except KeyboardInterrupt:
    print("프로그램 종료")

finally:

    pet_pwm.stop()
    can_pwm.stop()
    glass_pwm.stop()

    GPIO.cleanup()

    client_sock.close()

    server_sock.close()




        
    
















