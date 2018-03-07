import atexit

from flask import Flask, request, jsonify
from vehicles import Vehicle


LOCATIONS = {"backLeft": 1, "backRight": 2}
vehicle = Vehicle()
app = Flask(__name__)


def update_motor(location, motor):
    vehicle.update_motor(LOCATIONS[location], motor["command"]["value"], motor["speed"])


@app.route('/motors', methods=['PUT'])
def drive():
    app.logger.info("Received request to update motors: %s", request.json)

    for (location, motor) in request.json:
        update_motor(location, motor)

    return jsonify({})