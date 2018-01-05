from flask import Flask, request
from sense_hat import SenseHat

sense = SenseHat()
sense.set_imu_config(True, True, True)
sense.clear()
app = Flask(__name__)

initial_orientation = sense.orientation


@app.route('/')
def all_sensors():
    current_orientation = sense.orientation

    return str({
        "humidity": sense.humidity,
        "pressure": sense.pressure,
        "temperature": sense.temperature,
        "orientation": current_orientation,
        "relative_orientation": relative_orientation_dict(current_orientation),
        "compass": sense.compass,
        "magnetometer": sense.compass_raw,
        "gyroscope": sense.gyroscope_raw,
        "accelerometer": sense.accelerometer_raw,
    })


@app.route('/message', methods=['PUT'])
def message():
    text = request.json["message"]
    sense.clear()
    sense.show_message(text)
    return text


@app.route('/letter', methods=['PUT'])
def letter():
    text = request.json["letter"]
    sense.clear()
    sense.show_letter(text)
    return text


@app.route('/humidity')
def humidity():
    return str(sense.humidity)


@app.route('/pressure')
def pressure():
    return str(sense.pressure)


@app.route('/temperature')
def temperature():
    return str(sense.temperature)


@app.route('/orientation')
def orientation():
    return str(sense.orientation)


@app.route('/relative_orientation')
def relative_orientation():
    return str(relative_orientation_dict(sense.orientation))


def relative_orientation_dict(current_orientation):
    return {
        'yaw': relative_dimension("yaw", current_orientation),
        'pitch': relative_dimension("pitch", current_orientation),
        'roll': relative_dimension("roll", current_orientation)
    }


def relative_dimension(dimension, current_orientation):
    return initial_orientation[dimension] - current_orientation[dimension]


@app.route('/accelerometer')
def accelerometer():
    return str(sense.accelerometer_raw)


@app.route('/magnetometer')
def magnetometer():
    return str(sense.compass_raw)


@app.route('/compass')
def compass():
    return str(sense.compass)


@app.route('/gyroscope')
def gyroscope():
    return str(sense.gyroscope_raw)
