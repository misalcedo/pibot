import asyncio
from echo_pb2 import Envelope


class EchoClientProtocol:
    def __init__(self, loop):
        self.envelope = Envelope()
        self.envelope.message = "Hello World!"
        self.loop = loop
        self.transport = None

    def connection_made(self, transport):
        self.transport = transport
        print('Send:', self.envelope.message)
        self.transport.sendto(self.envelope.SerializeToString())

    def datagram_received(self, data, address):
        envelope = Envelope()
        envelope.ParseFromString(data)

        print("Received:", envelope.message)
        print("Close the socket")

        self.transport.close()

    def error_received(self, exc):
        print('Error received:', exc)

    def connection_lost(self, exception):
        print("Socket closed, stop the event loop")
        loop = asyncio.get_event_loop()
        loop.stop()


loop = asyncio.get_event_loop()
connect = loop.create_datagram_endpoint(lambda: EchoClientProtocol(loop), remote_addr=('server', 80))
transport, protocol = loop.run_until_complete(connect)
loop.run_forever()
transport.close()
loop.close()
