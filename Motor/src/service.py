import atexit
from flask import Flask, request, jsonify

from vehicles import Vehicle

LOCATIONS = {"backLeft": 1, "backRight": 2}
vehicle = Vehicle()
app = Flask(__name__)


@atexit.register
def release():
    vehicle.release()


def update_motor(location, motor):
    app.logger.info("Updating motor at %s with %s", location, motor)
    vehicle.update_motor(LOCATIONS[location], motor["command"]["value"], motor["speed"])


@app.route('/motors', methods=['PUT'])
def drive():
    for location in request.json:
        update_motor(location, request.json[location])

    return jsonify({})
