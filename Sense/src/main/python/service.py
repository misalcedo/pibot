from flask import Flask, Response, request
from sense_hat import SenseHat


sense = SenseHat()
sense.set_imu_config(True, True, True)
sense.clear()
app = Flask(__name__)


@app.route('/')
def all_sensors():
    return str({
        "humidity": sense.humidity,
        "pressure": sense.pressure,
        "temperature": sense.temperature,
        "orientation": sense.orientation,
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
    return str(sense.temperature * 9 / 5 + 32)


@app.route('/orientation')
def orientation():
    return str(sense.orientation)


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
