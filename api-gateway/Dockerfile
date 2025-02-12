# Use an official Python runtime as a parent image
FROM --platform=linux/arm64 python:3.9-slim

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt .

# Install any needed packages specified in requirements.txt
# by production -> need to run the command after run container
RUN pip install --no-cache-dir -r requirements.txt

# Copy the current directory contents into the container at /app
COPY src/main/python/app .

# Expose port 80 for the API Gateway
EXPOSE 80

# Run app.py when the container launches
CMD ["python", "app.py"]