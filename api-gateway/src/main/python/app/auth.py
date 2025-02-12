from flask import Blueprint, request, jsonify, redirect, url_for, session
from flask_jwt_extended import create_access_token, create_refresh_token, get_jwt_identity, jwt_required
from authlib.integrations.flask_client import OAuth
# from werkzeug.security import generate_password_hash
import psycopg2
import os
import random
import string

auth_bp = Blueprint('auth', __name__)

# PostgreSQL connection parameters
DATABASE_URL = os.getenv('DATABASE_URL', 'postgresql://pbrs_manager:faw392k892@postgres:5432/pbrs')

# Configure OAuth
oauth = OAuth()
auth_bp.config = {
    "GOOGLE_CLIENT_ID": "1039874584081-91pqta8vbm8ehhd1450g8q4h0eb4f853.apps.googleusercontent.com",
    "GOOGLE_CLIENT_SECRET": "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",
    # "JWT_SECRET_KEY": "your_jwt_secret_key",  # Used for JWT token signing
}
oauth.init_app(auth_bp)

google = oauth.register(
    name="google",
    client_id=auth_bp.config["GOOGLE_CLIENT_ID"],
    client_secret=auth_bp.config["GOOGLE_CLIENT_SECRET"],
    access_token_url="https://oauth2.googleapis.com/token",
    authorize_url="https://accounts.google.com/o/oauth2/auth",
    api_base_url="https://www.googleapis.com/oauth2/v1/",
    client_kwargs={"scope": "openid email profile"},
)

# Connect to PostgreSQL
def get_db_connection():
    conn = psycopg2.connect(DATABASE_URL)
    return conn

@auth_bp.route('/service/public/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')
    # password = generate_password_hash(password)

    if not username or not email or not password:
        return jsonify({'error': 'Missing fields!'}), 400

    conn = get_db_connection()
    cur = conn.cursor()

    # Check if the user already exists
    cur.execute("SELECT * FROM users WHERE username = %s", (username,))
    user = cur.fetchone()
    if user:
        return jsonify({'error': 'User already exists!'}), 400

    # Check if the user email already exists
    cur.execute("SELECT * FROM users WHERE email = %s", (email,))
    user = cur.fetchone()
    if user:
        return jsonify({'error': 'User email already exists!'}), 400

    # Insert into the users table
    cur.execute("INSERT INTO users (username, email, password) VALUES (%s, %s, %s)",
                (username, email, password))
    conn.commit()
    cur.close()
    conn.close()

    return jsonify({'msg': 'User created'}), 201

@auth_bp.route('/service/public/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')
    # password = generate_password_hash(password)
    conn = get_db_connection()
    cur = conn.cursor()

    # check user
    cur.execute("SELECT id FROM users WHERE email = %s and password = %s", (email, password))
    user = cur.fetchone()
    if not user:
        return jsonify({'error': 'Invalid credentials!'}), 401
    cur.close()
    conn.close()

    access_token = create_access_token(identity=email, additional_claims={"role": user.role.upper()})
    refresh_token = create_refresh_token(identity=email)
    user_id = user[0]
    return jsonify(access_token=access_token, refresh_token=refresh_token, user_id=user_id), 200
    # return jsonify(access_token=access_token), 200

@auth_bp.route('/service/public/refresh', methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    current_user = get_jwt_identity()
    # Create a new access token
    new_access_token = create_access_token(identity=current_user, additional_claims={"role": current_user.role.upper()})
    return jsonify(access_token=new_access_token)

@auth_bp.route("/service/public/login/google")
def login_with_google():
    # Redirect to Google's OAuth page
    redirect_uri = url_for("authorize_google", _external=True)
    return google.authorize_redirect(redirect_uri)

@auth_bp.route("/service/public/authorize/google")
def authorize_google():
    # Get the token and user info from Google
    token = google.authorize_access_token()
    user_info = google.get("userinfo").json()
    # user_id = ""

    # Extract email (or any other unique identifier)
    email = user_info["email"]
    name = user_info.get("name", email)  # Use name if available, fallback to email
    conn = get_db_connection()
    cur = conn.cursor()
    if name == email:
        name = ''.join(random.choices(string.ascii_letters, k=10))  # Generate random name
        username = cur.execute("SELECT username FROM users WHERE username = %s", (name,))
        while username:
            name = ''.join(random.choices(string.ascii_letters, k=10))
            username = cur.execute("SELECT username FROM users WHERE username = %s", (name,))

    # check user
    cur.execute("SELECT id FROM users WHERE email = %s", (email,))
    user = cur.fetchone()

    if not user:
        # If user doesn't exist, create a new record
        cur.execute("INSERT INTO users (username, email) VALUES (%s, %s)",
                    (name, email,))
        conn.commit()

        cur.execute("SELECT id FROM users WHERE email = %s", (email,))
        user = cur.fetchone()
        user_id = user[0]
    else:
        user_id = user[0]
    cur.close()
    conn.close()

    # Create JWT tokens
    access_token = create_access_token(identity=email, additional_claims={"role": user.role.upper()})
    refresh_token = create_refresh_token(identity=email)

    return jsonify({
        "msg": "Logged in with Google",
        "email": email,
        "name": name,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "user_id": user_id
    })