from picamera import PiCamera
from mjpeg import FrameSplitter
from stream import StreamingServer, BaseStreamingHandler


class StreamingHandler(BaseStreamingHandler):
    def send_frame(self):
        camera.capture(self.wfile, 'bmp', use_video_port=True)

    def send_frames(self):
        try:
            video_output.truncate()

            while True:
                frame = video_output.read()
                if frame:
                    self.wfile.write(b'--FRAME\r\n')
                    self.send_header('Content-Type', 'image/jpeg')
                    self.send_header('Content-Length', len(frame))
                    self.end_headers()
                    self.wfile.write(frame)
                    self.wfile.write(b'\r\n')
        except Exception as e:
            self.log_message('Removed streaming client %s: %s', self.client_address, str(e))


if __name__ == '__main__':
    with PiCamera() as camera:
        camera.hflip = True
        camera.vflip = True
        camera.start_preview()
        video_output = FrameSplitter()
        camera.start_recording(video_output, framerate=24, format='mjpeg')

        try:
            server = StreamingServer(('', 80), StreamingHandler)
            server.serve_forever()
        finally:
            camera.stop_recording()
