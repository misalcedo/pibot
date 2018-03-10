from Adafruit_MotorHAT import Adafruit_MotorHAT


class Vehicle:
    def __init__(self, motor_hat=Adafruit_MotorHAT()):
        self.motor_hat = motor_hat

    def release(self):
        self.motor_hat.getMotor(1).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(2).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(3).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(4).run(Adafruit_MotorHAT.RELEASE)

    def update_motor(self, index, command, speed):
        motor = self.motor_hat.getMotor(index)
        motor.run(command)
        motor.setSpeed(speed)