from Adafruit_MotorHAT import Adafruit_MotorHAT


class Vehicle:
    def __init__(self, motor_hat=None):
        self.motor_hat = motor_hat
        self.motors = []

    def release(self):
        self.motor_hat.getMotor(1).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(2).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(3).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(4).run(Adafruit_MotorHAT.RELEASE)

    def update_motor(self, index, command, speed):
        with self.motor_hat.getMotor(index) as motor:
            motor.run(command)
            motor.setSpeed(speed)

        motor = {"location": index, "command": command, "speed": speed}
        n = len(self.motors)

        if index < n:
            self.motors[index] = motor
        elif index == n:
            self.motors.append(motor)
        else:
            raise IndexError()
