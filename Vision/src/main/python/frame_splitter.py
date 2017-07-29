from io import BytesIO
from threading import Lock


class FrameSplitter:
    def __init__(self):
        self.stream = BytesIO()
        self.lock = Lock()
        self.frame = None

    def write(self, buffer):
        if buffer.startswith(b'\xff\xd8'):
            # Start of new frame
            size = self.stream.tell()
            if size > 0:
                self.stream.seek(0)
                with self.lock:
                    self.frame = self.stream.read(size)
                self.stream.seek(0)

        self.stream.write(buffer)

    def copy_to(self, buffer):
        with self.lock:
            if self.frame:
                buffer.write(self.frame)
                self.frame = None

