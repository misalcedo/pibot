from flask import Flask, request, Response, send_file
from picamera import PiCamera
from io import BytesIO

camera = PiCamera()
camera.vflip = True
camera.start_preview()
app = Flask(__name__)


@app.route('/capture', methods=['GET'])
def capture():
    def generate():
        stream = BytesIO()

        camera.capture(stream, 'bmp')
        stream.seek(0)

        yield stream.read()

    return Response(generate(), mimetype='image/bmp')


@app.route('/preview', methods=['GET'])
def preview():
    stream = BytesIO()

    def generate():
        for _ in camera.capture_continuous(stream, 'jpeg'):
            stream.seek(0)

            yield b'--frame\r\n'
            yield b'Content-Type: image/jpeg\r\n\r\n'
            yield stream.read()
            yield b'\r\n'

            # Reset the stream for the next capture
            stream.seek(0)
            stream.truncate()

    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=False)
