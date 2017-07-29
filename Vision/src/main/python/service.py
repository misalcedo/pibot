from flask import Flask, request, Response, send_file
from io import BytesIO
from camera import Camera
from picamera import PiCameraCircularIO


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
    try:
        Camera().stop_recording()
    except:
        pass

    def generate():
        with PiCameraCircularIO(Camera(), seconds=30) as stream:
            Camera().start_recording(stream, format='mjpeg')

            while True:
                for frame in stream.frames:
                    if frame.complete:
                        with stream.lock:
                            stream.seek(frame.position)
                            contents = stream.read(frame.frame_size)
                            stream.seek(0, whence=2) # end of stream

                        if contents:
                            yield b'--FRAME\r\n'
                            yield b'Content-Type: image/jpeg\r\n\r\n'
                            yield contents
                            yield b'\r\n'

                stream.clear()

    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=FRAME')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True, threaded=True)
