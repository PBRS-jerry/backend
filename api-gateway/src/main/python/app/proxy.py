import requests

from flask import request, jsonify, Response

class Proxy:
    def __init__(self, app):
        self.app = app


    def forward_request(self, service_url, original_request):
        # Forward the request to the specified service URL
        global response
        method = original_request.method
        headers = {key: value for key, value in original_request.headers if key != "Host"}
        headers['Content-Length'] = str(len(request.data))  # Force Content-Length
        # Forward the request to the service
        try:
            if method == 'GET':
                response = requests.get(service_url, headers=headers)
            elif method == 'POST':
                response = requests.post(service_url, headers=headers, json=original_request.get_json())
            elif method == 'PUT':
                # response = requests.put(service_url, headers=headers, json=original_request.get_json())
                response = requests.put(service_url, headers=headers, data=request.data)
            elif method == 'DELETE':
                response = requests.delete(service_url, headers=headers)
            # Return the response from the service
            # return (response.content, response.status_code, response.headers.items())

            response_headers = dict(response.headers)
            response_headers.pop('Transfer-Encoding', None)  # Remove chunked encoding if present

            # Forward the response back to the client
            flask_response = Response(
                response.content,
                status=response.status_code,
                headers=response_headers  # Preserve headers from Spring
            )
            return flask_response

        except requests.exceptions.RequestException as e:
            return jsonify({"error": str(e)}), 500