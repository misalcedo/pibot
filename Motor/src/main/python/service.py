import atexit

from flask import Flask, request, jsonify
from vehicles import Vehicle


vehicle = Vehicle()
app = Flask(__name__)


def update_motor(motor):
    vehicle.update_motor(int(motor["location"]), int(motor["command"]), int(motor["speed"]))


@app.route('/motors', methods=['PUT'])
def drive():
    app.logger.info("Received request to update motors: %s", request.json)

    for motor in request.json.values():
        update_motor(motor)

    return jsonify(vehicle.dict())


@app.route('/release', methods=['PUT'])
@atexit.register
def release():
    vehicle.release()
    return jsonify(vehicle.dict())


@app.route('/motors', methods=['GET'])
def state():
    return jsonify({'backLeft': vehicle.motors[0], 'backRight': vehicle.motors[1]})
