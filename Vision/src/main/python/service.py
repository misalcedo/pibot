from flask import Flask, request, Response, send_file
from io import BytesIO
from camera import Camera
from frame_splitter import FrameSplitter


app = Flask(__name__)


@app.route('/capture', methods=['GET'])
def capture():
    def generate():
        stream = BytesIO()

        Camera().capture(stream, 'bmp', use_video_port=True)

        stream.seek(0)

        yield stream.read()

    return Response(generate(), mimetype='image/bmp')


@app.route('/preview', methods=['GET'])
def preview():
    def generate():
        frame_splitter = FrameSplitter()
        camera = Camera()
        camera.start_recording(frame_splitter, format='mjpeg')

        while True:
            frame = frame_splitter.read()
            if frame:
                yield b'--FRAME\r\n'
                yield b'Content-Type: image/jpeg\r\n\r\n'
                yield frame
                yield b'\r\n'

    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=FRAME')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True, threaded=True)
