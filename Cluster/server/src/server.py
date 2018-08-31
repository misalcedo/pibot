import asyncio
from echo_pb2 import Envelope


class EchoServerProtocol:
    def connection_made(self, transport):
        self.transport = transport

    def datagram_received(self, data, address):
        envelope = Envelope()
        envelope.ParseFromString(data)

        print('Received %r from %s' % (envelope.message, address))
        print('Send %r to %s' % (envelope.message, address))

        self.transport.sendto(data, address)


loop = asyncio.get_event_loop()
print("Starting UDP server")
# One protocol instance will be created to serve all client requests
listen = loop.create_datagram_endpoint(EchoServerProtocol, local_addr=('0.0.0.0', 80))
transport, protocol = loop.run_until_complete(listen)

try:
    loop.run_forever()
except KeyboardInterrupt:
    pass

transport.close()
loop.close()
