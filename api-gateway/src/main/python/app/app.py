from functools import wraps

from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_jwt_extended import jwt_required, get_jwt
from auth import auth_bp
from middleware import init_jwt
from config import Config
from proxy import Proxy
import logging
from logging.handlers import RotatingFileHandler
import util.app_logger

logger = util.app_logger.AppLogger("api-gateway", log_dir="log")

# # Set up logging
# logging.basicConfig(
#     filename='app.log',
#     level=logging.DEBUG,
#     format='%(asctime)s %(levelname)s: %(message)s'
# )
# logger = logging.getLogger(__name__)

app = Flask(__name__)
# CORS(app)  # Enable CORS if needed
CORS(app, resources={
    r"/*": {
        "origins": ["http://localhost:9011", "http://pbrs-fe-dev:5173"],  # Add both host and container URLs
        "methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})

# Add logging middleware
# @app.before_request
# def log_request_info():
#     logger.debug('Headers: %s', request.headers)
#     logger.debug('Body: %s', request.get_data())
#     logger.debug('URL: %s', request.url)

app.config.from_object(Config)

# Initialize JWT
init_jwt(app)

# Register auth blueprint
app.register_blueprint(auth_bp)

# Initialize Proxy
proxy = Proxy(app)

# first page
@app.route('/', methods=['GET'])
def home():
    return "Welcome to the API Gateway!"

def role_required(role):
    """Decorator to restrict access based on role"""
    def wrapper(fn):
        @wraps(fn)
        def decorated_function(*args, **kwargs):
            claims = get_jwt()
            if claims.get("role") != role:
                return jsonify({"error": "Forbidden"}), 403
            return fn(*args, **kwargs)
        return decorated_function
    return wrapper

# Define unprotected route
@app.route('/service/public/<path:path>', methods=['GET', 'POST'])
def unprotected_service_route(path):
    return proxy.forward_request('http://pbrs-api-gateway:5000/service/public/' + path, request)

# Define protected route that forwards requests
@app.route('/service/protected/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
@jwt_required()
def protected_service_route(path):
    return proxy.forward_request('http://pbrs-api-gateway:5000/service/protected/' + path, request)

# Example of a protected route to another service
@app.route('/user-service/public/<path:path>', methods=['GET'])
def unprotected_user_service_route(path):
    return proxy.forward_request('http://pbrs-user-service:8080/user-service/public/' + path, request)

# Define another protected route for a different service
@app.route('/user-service/protected/<path:path>', methods=['GET', 'POST', 'PUT', 'DELETE'])
@jwt_required()
def protected_user_service_route(path):
    return proxy.forward_request('http://pbrs-user-service:8080/user-service/protected/' + path, request)

@app.route("/book-service/public/<path:path>", methods=['GET', 'POST'])
def unprotected_book_service_route(path):
    return proxy.forward_request('http://pbrs-book-service:8080/book-service/public/' + path, request)

@app.route("/book-service/protected", methods=['POST', 'PUT', 'DELETE'])
@jwt_required()
@role_required("ADMIN")
def add_book():
    return proxy.forward_request('http://pbrs-book-service:8080/book-service/protected/' + path, request)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
