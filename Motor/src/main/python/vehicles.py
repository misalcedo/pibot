from Adafruit_MotorHAT import Adafruit_MotorHAT


class Vehicle:
    def __init__(self, motor_hat=Adafruit_MotorHAT()):
        self.motor_hat = motor_hat
        self.motors = []

    def release(self):
        self.motor_hat.getMotor(1).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(2).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(3).run(Adafruit_MotorHAT.RELEASE)
        self.motor_hat.getMotor(4).run(Adafruit_MotorHAT.RELEASE)

    def update_motor(self, index, command, speed):
        motor = self.motor_hat.getMotor(index + 1)
        motor.run(command)
        motor.setSpeed(speed)

        motor_state = {"location": index, "command": command, "speed": speed}
        n = len(self.motors)

        if index < n:
            self.motors[index] = motor_state
        elif index == n:
            self.motors.append(motor_state)
        else:
            raise IndexError()

    def dict(self):
        return {"motors": self.motors}