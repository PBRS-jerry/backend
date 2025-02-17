import logging
import functools
import traceback
from datetime import datetime
from logging.handlers import RotatingFileHandler
import os

class AppLogger:
    def __init__(self, app_name: str, log_dir: str = "logs"):
        self.app_name = app_name
        self.log_dir = log_dir

        # Create logs directory if it doesn't exist
        if not os.path.exists(log_dir):
            os.makedirs(log_dir)

        # Configure logging
        self.logger = logging.getLogger(app_name)
        self.logger.setLevel(logging.INFO)

        # Create formatters
        file_formatter = logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - [%(filename)s:%(lineno)d] - %(message)s'
        )
        console_formatter = logging.Formatter(
            '%(asctime)s - %(levelname)s - %(message)s'
        )

        # File handler for all logs
        all_handler = RotatingFileHandler(
            f"{log_dir}/{app_name}_all.log",
            maxBytes=10000000,  # 10MB
            backupCount=5
        )
        all_handler.setFormatter(file_formatter)

        # File handler for errors only
        error_handler = RotatingFileHandler(
            f"{log_dir}/{app_name}_error.log",
            maxBytes=10000000,  # 10MB
            backupCount=5
        )
        error_handler.setLevel(logging.ERROR)
        error_handler.setFormatter(file_formatter)

        # Console handler
        console_handler = logging.StreamHandler()
        console_handler.setFormatter(console_formatter)

        # Add handlers
        self.logger.addHandler(all_handler)
        self.logger.addHandler(error_handler)
        self.logger.addHandler(console_handler)

    def info(self, message: str, **kwargs):
        """Log info level message"""
        extra = ', '.join(f'{k}={v}' for k, v in kwargs.items())
        self.logger.info(f"{message} {extra}".strip())

    def error(self, message: str, exc_info=None, **kwargs):
        """Log error level message"""
        extra = ', '.join(f'{k}={v}' for k, v in kwargs.items())
        if exc_info:
            self.logger.error(f"{message} {extra}".strip(), exc_info=exc_info)
        else:
            self.logger.error(f"{message} {extra}".strip())

    def warning(self, message: str, **kwargs):
        """Log warning level message"""
        extra = ', '.join(f'{k}={v}' for k, v in kwargs.items())
        self.logger.warning(f"{message} {extra}".strip())

    def debug(self, message: str, **kwargs):
        """Log debug level message"""
        extra = ', '.join(f'{k}={v}' for k, v in kwargs.items())
        self.logger.debug(f"{message} {extra}".strip())

    def log_exception(self, exception: Exception):
        """Log an exception with full traceback"""
        error_msg = f"Exception occurred: {str(exception)}"
        self.error(error_msg, exc_info=True)

    def log_decorator(self, func):
        """Decorator to log function entry/exit and exceptions"""
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            func_name = func.__name__
            try:
                self.info(f"Entering function: {func_name}")
                result = func(*args, **kwargs)
                self.info(f"Exiting function: {func_name}")
                return result
            except Exception as e:
                self.log_exception(e)
                raise
        return wrapper