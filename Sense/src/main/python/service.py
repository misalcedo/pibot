from flask import Flask, Response
from io import BytesIO

from camera import create_camera
from mjpeg import FrameSplitter

PAGE = """\
    <html>
        <head>
        <title>RapBot MJPEG Stream</title>
        </head>
        <body>
            <h1>RapBot MJPEG Stream</h1>
            <img src="stream.mjpg"/>
        </body>
    </html>
    """.encode('utf-8')

app = Flask(__name__)
video_output = FrameSplitter()
camera = create_camera(video_output)


@app.route("/")
def root():
    return PAGE


@app.route("/still.jpg")
def still():
    def generate(buffer):
        camera.capture(buffer, format='jpeg', use_video_port=True)

        buffer.seek(0)

        yield buffer.read()

    return Response(generate(BytesIO()), mimetype='image/jpeg')


@app.route("/stream.mjpg")
def stream():
    def generate():
        video_output.truncate()

        while True:
            frame = video_output.read()
            if frame:
                yield b'--FRAME\r\n'
                yield b'Content-Type: image/jpeg\r\n\r\n'
                yield frame
                yield b'\r\n'

    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=FRAME')
