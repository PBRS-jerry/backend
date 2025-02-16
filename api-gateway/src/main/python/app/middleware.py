from flask import jsonify
from flask_jwt_extended import JWTManager, jwt_required

def init_jwt(app):
    jwt = JWTManager(app)

    @jwt.unauthorized_loader
    def unauthorized_response(err):
        return jsonify({'msg': 'Missing Authorization Header'}), 401

    @jwt.expired_token_loader
    def expired_token_response(jwt_header, jwt_payload):
        # return jsonify({'msg': 'Token has expired'}), 401
        return jsonify({
            "msg": "The access token has expired",
            "error": "token_expired",
            "refresh_url": "/service/public/refresh"  # Provide the client with the refresh endpoint
        }), 401

    @jwt.invalid_token_loader
    def invalid_token_response(err):
        return jsonify({'msg': 'Invalid token'}), 401