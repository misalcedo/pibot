from picamera import PiCamera
from mjpeg import FrameSplitter
from stream import StreamingServer, BaseStreamingHandler


class StreamingHandler(BaseStreamingHandler):
    @staticmethod
    def read_frame():
        with video_output.condition:
            video_output.condition.wait()
            return video_output.frame

    def send_frame(self):
        self.wfile.write(self.read_frame())

    def send_frames(self):
        try:
            while True:
                frame = self.read_frame()

                self.wfile.write(b'--FRAME\r\n')
                self.send_header('Content-Type', 'image/jpeg')
                self.send_header('Content-Length', len(frame))
                self.end_headers()
                self.wfile.write(frame)
                self.wfile.write(b'\r\n')
        except Exception as e:
            self.log_message('Removed streaming client %s: %s', self.client_address, str(e))


if __name__ == '__main__':
    with PiCamera(framerate=24) as camera:
        camera.hflip = True
        camera.vflip = True
        camera.start_preview()
        video_output = FrameSplitter()
        camera.start_recording(video_output, format='mjpeg')

        try:
            server = StreamingServer(('', 80), StreamingHandler)
            server.serve_forever()
        finally:
            camera.stop_recording()
