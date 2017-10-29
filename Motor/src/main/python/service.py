import atexit

from Adafruit_MotorHAT import Adafruit_MotorHAT
from flask import Flask, request

# create a default object, no changes to I2C address or frequency
LOCATION_TO_INDEX = {"BACK_RIGHT": 1, "BACK_LEFT": 2}
COMMAND_MAP = {
    "FORWARD": Adafruit_MotorHAT.FORWARD,
    "BACKWARD": Adafruit_MotorHAT.BACKWARD,
    "BRAKE": Adafruit_MotorHAT.BRAKE,
    "RELEASE": Adafruit_MotorHAT.RELEASE
}

motor_hat = Adafruit_MotorHAT()


# recommended for auto-disabling motors on shutdown!
@atexit.register
def turn_off_motors():
    motor_hat.getMotor(1).run(Adafruit_MotorHAT.RELEASE)
    motor_hat.getMotor(2).run(Adafruit_MotorHAT.RELEASE)
    motor_hat.getMotor(3).run(Adafruit_MotorHAT.RELEASE)
    motor_hat.getMotor(4).run(Adafruit_MotorHAT.RELEASE)


app = Flask(__name__)


def update_motor(key, value):
    index = LOCATION_TO_INDEX[key]
    command = value["command"]
    speed = value["speed"]

    app.logger.debug("Updating %s motor with index %d to speed: %d, command: %s", key, index, speed, command)

    motor = motor_hat.getMotor(index)
    motor.run(COMMAND_MAP[command])
    motor.setSpeed(speed)


@app.route('/motors', methods=['PUT'])
def drive():
    for key, value in request.json["motors"].items():
        update_motor(key, value)
