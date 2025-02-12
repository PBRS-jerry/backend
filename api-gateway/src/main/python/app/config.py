import os

class Config:
    SECRET_KEY = os.getenv("SECRET_KEY", "sdklifg98023j4f")
    JWT_ACCESS_TOKEN_EXPIRES = 3600
    JWT_REFRESH_TOKEN_EXPIRES = 864000
    JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "sdklifg98023j4f")
    GOOGLE_CLIENT_ID = os.getenv("GOOGLE_CLIENT_ID", "1039874584081-91pqta8vbm8ehhd1450g8q4h0eb4f853.apps.googleusercontent.com")
    GOOGLE_CLIENT_SECRET = os.getenv("GOOGLE_CLIENT_SECRET", "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")