from io import BytesIO
from collections import deque


class FrameSplitter:
    def __init__(self):
        self.output = BytesIO()
        self.queue = deque()

    def write(self, buffer):
        if buffer.startswith(b'\xff\xd8'):
            size = self.output.tell()
            self.output.seek(0)

            if size > 0:
                self.queue.append(self.output.read(size))
                self.output.seek(0)

        self.output.write(buffer)

    def read(self):
        try:
            return self.queue.pop()
        except IndexError:
            return None