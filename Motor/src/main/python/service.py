import atexit

from Adafruit_MotorHAT import Adafruit_MotorHAT
from flask import Flask
from flask_restful import Resource, Api, reqparse, abort

# create a default object, no changes to I2C address or frequency
motor_hat = Adafruit_MotorHAT()


# recommended for auto-disabling motors on shutdown!
def turn_off_motors():
    motor_hat.getMotor(1).run(Adafruit_MotorHAT.RELEASE)
    motor_hat.getMotor(2).run(Adafruit_MotorHAT.RELEASE)
    motor_hat.getMotor(3).run(Adafruit_MotorHAT.RELEASE)
    motor_hat.getMotor(4).run(Adafruit_MotorHAT.RELEASE)


atexit.register(turn_off_motors)

app = Flask(__name__)
api = Api(app)

parser = reqparse.RequestParser()
parser.add_argument('speed', type=int, help='Speed of the motor.', required=True)
parser.add_argument('command', type=int, help='Sets the direction of the rotation for the motor.', required=True)


class Motor(Resource):
    def put(self, motor_id):
        self.validate_existence(motor_id)
        arguments = self.parse_arguments()

        motor = motor_hat.getMotor(motor_id)
        motor.run(arguments['command'])
        motor.setSpeed(arguments['speed'])

        return {'motor_id': motor_id, 'state': arguments}

    @staticmethod
    def validate_existence(motor_id):
        if motor_id not in range(1, 5):
            abort(404, message="Motor {} doesn't exist.".format(motor_id))

    @staticmethod
    def parse_arguments():
        return parser.parse_args(strict=True)


api.add_resource(Motor, '/motor/<int:motor_id>')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True)
