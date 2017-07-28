import atexit
from picamera import PiCamera


class Camera:
    _instance = None

    @staticmethod
    def singleton():
        if Camera._instance is None:
            Camera._instance = PiCamera()
            Camera._instance.hflip = True
            Camera._instance.vflip = True
            Camera._instance.start_preview()

        return Camera._instance

    @staticmethod
    @atexit.register
    def close():
        if Camera._instance is not None:
            Camera._instance.close()