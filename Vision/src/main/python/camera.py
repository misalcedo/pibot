from picamera import PiCamera


def create_camera(buffer):
    pi_camera = PiCamera()

    pi_camera.hflip = True
    pi_camera.vflip = True
    pi_camera.start_preview()
    pi_camera.start_recording(buffer, format='mjpeg')

    return pi_camera
