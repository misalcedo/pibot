import atexit
from picamera import PiCamera


class Camera:
    instance = None

    def __init__(self):
        if Camera.instance is None:
            self.instance = Camera.instance = PiCamera()
            self.instance.hflip = True
            self.instance.vflip = True
            self.instance.start_preview()
        else:
            self.instance = Camera.instance

    def __getattr__(self, name):
        return getattr(self.instance, name)


@atexit.register
def close():
    if Camera.instance is not None:
        Camera.instance.close()