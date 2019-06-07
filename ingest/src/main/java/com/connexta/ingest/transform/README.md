# Transform Service Client
This package includes classes to send requests to, and receive
responses from, the transform service.

To use the *TransformClient*, inject it into the controller or class that wants
to communicate with the transform service. Create a new instance of *TransformRequest*
and pass it to the TransformClient"s *requestTransform*. The method responds with an
instance of *TransformResponse*.

The response from the transform service includes a JSON object for in the case of
success, or for certain error status codes. The JSON is used to populate the fields of the
*TransformResponse* If the response includes no JSON, the *TransformResponse* only contains
the ID attribute used in the *TransformRequest* and the HTTP status of the response.

The *TransformClient* does not bubble up exceptions for unsuccessful transform
requests. Instead, use the isError() method on the *TransformRequest* to determine
success or failure.
