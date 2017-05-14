from flask import Flask
from flask_restful import Resource, Api

app = Flask(__name__)
api = Api(app)

class Motor(Resource):
    def get(self):
        return 'Hello, World!'

api.add_resource(Motor, '/')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True)